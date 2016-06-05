package ch.rasc.vision.eventbus;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(visibility = ImplementationVisibility.PACKAGE)
@Value.Immutable(copy = false, builder = false)
public interface EventBusEvent {
	@Value.Parameter
	String name();

	@Value.Parameter
	@Nullable
	Object data();

	public static EventBusEvent of(String name) {
		return ImmutableEventBusEvent.of(name, null);
	}

	public static EventBusEvent of(String name, Object data) {
		return ImmutableEventBusEvent.of(name, data);
	}
}
