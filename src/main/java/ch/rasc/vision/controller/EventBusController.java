package ch.rasc.vision.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ch.rasc.sse.eventbus.SseEvent;
import ch.rasc.sse.eventbus.SseEventBus;

@RestController
public class EventBusController {

	private final SseEventBus eventBus;

	public EventBusController(SseEventBus eventBus) {
		this.eventBus = eventBus;
	}

	@GetMapping("/eventbus/{id}")
	public SseEmitter eventbus(@PathVariable("id") String id) {
		return eventBus.createSseEmitter(id, "imageadded");
	}

}
