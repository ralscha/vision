package ch.rasc.vision.eventbus;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Value.Style(visibility = ImplementationVisibility.PACKAGE)
@Value.Immutable(copy = false, builder = false)
public interface EventBusClient {

	@Value.Parameter
	String id();

	@Value.Parameter
	SseEmitter emitter();

	public static EventBusClient of(String id, SseEmitter emitter) {
		return ImmutableEventBusClient.of(id, emitter);
	}
}