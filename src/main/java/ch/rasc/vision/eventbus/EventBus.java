package ch.rasc.vision.eventbus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rasc.vision.Application;
import net.jodah.expiringmap.ExpiringMap;

@Component
public class EventBus {

	private final Map<String, EventBusClient> clients;

	private final Map<String, Integer> failedClients;

	private final Map<String, Set<String>> eventSubscribers;

	private final Map<String, List<EventBusEvent>> pendingEvents;

	private final ObjectMapper objectMapper;

	@Autowired
	public EventBus(ObjectMapper objectMapper) {
		this.clients = ExpiringMap.builder().expiration(1, TimeUnit.DAYS)
				.expirationListener(this::expirationListener).build();
		this.objectMapper = objectMapper;
		this.failedClients = new ConcurrentHashMap<>();
		this.eventSubscribers = new ConcurrentHashMap<>();
		this.pendingEvents = new ConcurrentHashMap<>();
	}

	private void expirationListener(final String clientId,
			@SuppressWarnings("unused") final EventBusClient client) {
		Set<String> emptyEvents = new HashSet<>();
		for (Map.Entry<String, Set<String>> entry : this.eventSubscribers.entrySet()) {
			Set<String> clientIds = entry.getValue();
			clientIds.remove(clientId);
			if (clientIds.isEmpty()) {
				emptyEvents.add(entry.getKey());
			}
		}
		emptyEvents.forEach(this.eventSubscribers::remove);
	}

	public void registerClient(EventBusClient client) {
		this.clients.put(client.id(), client);
		this.failedClients.remove(client.id());
	}

	public void subscribe(String clientId, String event) {
		this.eventSubscribers.computeIfAbsent(event, k -> new HashSet<>()).add(clientId);
	}

	public void unsubscribeAll(String clientId) {
		this.expirationListener(clientId, null);
		this.clients.remove(clientId);
	}

	public void unsubscribe(String clientId, String event) {
		Set<String> clientIds = this.eventSubscribers.get(event);
		if (clientIds != null) {
			clientIds.remove(clientId);
			if (clientIds.isEmpty()) {
				this.eventSubscribers.remove(event);
			}
		}
	}

	@EventListener
	public void handleEvent(EventBusEvent event) {
		this.pendingEvents.computeIfAbsent(event.name(), k -> new ArrayList<>())
				.add(event);
	}

	@Scheduled(fixedDelay = 200)
	public void eventLoop() {
		if (EventBus.this.eventSubscribers.isEmpty()) {
			EventBus.this.pendingEvents.clear();
			return;
		}

		Iterator<Entry<String, List<EventBusEvent>>> it = EventBus.this.pendingEvents
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<EventBusEvent>> entry = it.next();
			it.remove();
			sendMessages(entry.getKey(), entry.getValue());
		}
	}

	private void sendMessages(String eventName, List<EventBusEvent> events) {
		Set<String> clientIds = EventBus.this.eventSubscribers.get(eventName);
		if (clientIds == null || clientIds.isEmpty()) {
			return;
		}

		List<Object> datas = new ArrayList<>();
		for (EventBusEvent event : events) {
			if (!event.combine()) {
				datas.clear();
			}
			if (event.data() != null) {
				datas.add(event.data());
			}
			else {
				datas.add("");
			}
		}

		SseEventBuilder sseBuilder = SseEmitter.event().name(eventName);
		try {
			if (datas.size() == 1) {
				sseBuilder.data(this.objectMapper.writeValueAsString(datas.get(0)));
			}
			else {
				sseBuilder.data(this.objectMapper.writeValueAsString(datas));
			}
		}
		catch (JsonProcessingException e) {
			Application.logger.error("event bus publish", e);
		}

		for (Map.Entry<String, EventBusClient> entry : EventBus.this.clients.entrySet()) {
			EventBusClient client = entry.getValue();
			try {
				client.emitter().send(sseBuilder);
			}
			catch (Exception e) {
				client.emitter().completeWithError(e);
				EventBus.this.failedClients.merge(entry.getKey(), 1, (v, vv) -> v + 1);
			}
		}

		EventBus.this.failedClients.entrySet().stream().filter(e -> e.getValue() > 20)
				.forEach(e -> {
					String clientId = e.getKey();
					unsubscribeAll(clientId);
					EventBus.this.failedClients.remove(clientId);
				});
	}
}
