package io.github.slimeistdev.volare.registry;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.GliderItem;
import io.github.slimeistdev.volare.content.glider.components.GliderParticlesComponent;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings({"SameParameterValue", "unused"})
public class VolareItems {
	public static final GliderItem GLIDER = register(
		"glider",
		GliderItem.create(VolareEntities.GLIDER),
		new Item.Settings()
			.maxCount(1)
			.component(VolareDataComponentTypes.GLIDER_PARTICLES, new GliderParticlesComponent(List.of(
				ParticleTypes.CHERRY_LEAVES,
				ParticleTypes.END_ROD,
				ParticleTypes.GLOW
			)))
	);

	private static <T extends Item> T register(String id, Function<Item.Settings, T> factory) {
		return register(id, factory, new Item.Settings());
	}

	private static <T extends Item> T register(String id, Function<Item.Settings, T> factory, Item.Settings settings) {
		var key = Volare.key(RegistryKeys.ITEM, id);
		T item = factory.apply(settings.registryKey(key));
		return Registry.register(Registries.ITEM, key, item);
	}

	public static void init() {}
}
