package ch.rasc.vision.eventbus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class EventBusController {

	private final EventBus eventBus;

	public EventBusController(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@GetMapping("/eventbus/{id}")
	public SseEmitter eventbus(@PathVariable("id") String id) {
		SseEmitter emitter = new SseEmitter(180_000L);
		emitter.onTimeout(emitter::complete);
		emitter.onCompletion(emitter::complete);

		this.eventBus.registerClient(EventBusClient.of(id, emitter));

		return emitter;
	}

	@GetMapping("/eventbus/subscribe/{id}/{event}")
	public void subscribe(@PathVariable("id") String id,
			@PathVariable("event") String event) {
		this.eventBus.subscribe(id, event);
	}

	@GetMapping("/eventbus/unsubscribe/{id}/{event}")
	public void unsubscribe(@PathVariable("id") String id,
			@PathVariable("event") String event) {
		this.eventBus.unsubscribe(id, event);
	}

	@GetMapping("/eventbus/unsubscribe/{id}")
	public void unsubscribeAll(@PathVariable("id") String id) {
		this.eventBus.unsubscribeAll(id);
	}

}
