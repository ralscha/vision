package ch.rasc.vision.eventbus;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rasc.vision.Application;
import net.jodah.expiringmap.ExpiringMap;

@Component
public class EventBus {

	private final Map<String, EventBusClient> clients;

	private final Map<String, Set<String>> events;

	private final ObjectMapper objectMapper;

	@Autowired
	public EventBus(ObjectMapper objectMapper) {
		this.clients = ExpiringMap.builder().expiration(1, TimeUnit.DAYS)
				.expirationListener(this::expirationListener).build();
		this.events = new ConcurrentHashMap<>();
		this.objectMapper = objectMapper;
	}

	private void expirationListener(final String clientId,
			@SuppressWarnings("unused") final EventBusClient client) {
		Set<String> emptyEvents = new HashSet<>();
		for (Map.Entry<String, Set<String>> entry : this.events.entrySet()) {
			Set<String> clientIds = entry.getValue();
			clientIds.remove(clientId);
			if (clientIds.isEmpty()) {
				emptyEvents.add(entry.getKey());
			}
		}
		emptyEvents.forEach(this.events::remove);
	}

	public void registerClient(EventBusClient client) {
		this.clients.put(client.id(), client);
	}

	public void subscribe(String clientId, String event) {
		this.events.computeIfAbsent(event, k -> new HashSet<>()).add(clientId);
	}

	public void unsubscribeAll(String clientId) {
		this.expirationListener(clientId, null);
		this.clients.remove(clientId);
	}

	public void unsubscribe(String clientId, String event) {
		Set<String> clientIds = this.events.get(event);
		if (clientIds != null) {
			clientIds.remove(clientId);
			if (clientIds.isEmpty()) {
				this.events.remove(event);
			}
		}
	}

	@EventListener
	public void handleEvent(EventBusEvent event) {
		Set<String> clientIds = this.events.get(event.name());
		if (clientIds == null || clientIds.isEmpty()) {
			return;
		}

		Set<String> failedClients = new HashSet<>();

		String data = null;
		try {
			data = this.objectMapper.writeValueAsString(event.data());
		}
		catch (JsonProcessingException e) {
			Application.logger.error("event bus publish", e);
		}

		for (String clientId : clientIds) {
			EventBusClient client = this.clients.get(clientId);
			if (client != null) {
				try {
					client.emitter()
							.send(SseEmitter.event().name(event.name()).data(data));
				}
				catch (Exception e) {
					failedClients.add(client.id());
				}
			}
			else {
				failedClients.add(clientId);
			}
		}

		failedClients.forEach(this.clients::remove);
	}

}
