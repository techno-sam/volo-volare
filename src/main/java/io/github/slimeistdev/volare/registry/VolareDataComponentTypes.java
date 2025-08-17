package io.github.slimeistdev.volare.registry;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.components.GliderFrozenMotionComponent;
import io.github.slimeistdev.volare.content.glider.components.GliderParticlesComponent;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class VolareDataComponentTypes {
	public static final ComponentType<GliderParticlesComponent> GLIDER_PARTICLES = register(
		"glider/particles",
		b -> b
			.codec(GliderParticlesComponent.CODEC)
			.packetCodec(GliderParticlesComponent.PACKET_CODEC)
			.cache()
	);

	public static final ComponentType<GliderFrozenMotionComponent> GLIDER_FROZEN_MOTION = register(
		"glider/frozen_motion",
		b -> b
			.codec(GliderFrozenMotionComponent.CODEC)
			.packetCodec(GliderFrozenMotionComponent.PACKET_CODEC)
	);

	@SuppressWarnings("SameParameterValue")
	private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
		return Registry.register(Registries.DATA_COMPONENT_TYPE, Volare.id(id), builderOperator.apply(ComponentType.builder()).build());
	}

	public static void init() {
		ComponentTooltipAppenderRegistry.addAfter(DataComponentTypes.LORE, GLIDER_FROZEN_MOTION);
		ComponentTooltipAppenderRegistry.addAfter(DataComponentTypes.LORE, GLIDER_PARTICLES);
	}
}
