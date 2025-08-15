package io.github.slimeistdev.volare.compat.area_lib;

import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.registry.AreaDataComponentTypeRegistry;
import io.github.slimeistdev.volare.Volare;

import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public class AreaLibComponents {
	public static final AreaDataComponentType<FlakComponent> FLAK = registerTracking(FlakComponent::instance, "flak");
	public static final AreaDataComponentType<ThermalComponent> THERMAL = registerTracking(ThermalComponent::new, "thermal");

	private static <T extends AreaDataComponent> AreaDataComponentType<T> registerTracking(Supplier<T> factory, String path) {
		return AreaDataComponentTypeRegistry.registerTracking(Volare.id(path), factory);
	}

	public static void init() {}
}
