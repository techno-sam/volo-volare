package io.github.slimeistdev.volare.registry;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.GliderEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;

public class VolareEntities {
	public static final EntityType<GliderEntity> GLIDER = register(
		"glider",
		EntityType.Builder.create(GliderEntity.create(() -> VolareItems.GLIDER), SpawnGroup.MISC)
			.dropsNothing()
			.dimensions(1.375f, 0.25f)
			.eyeHeight(0.25f)
			.maxTrackingRange(10)
	);

	public static void init() {}

	@SuppressWarnings("SameParameterValue")
	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		var key = Volare.key(RegistryKeys.ENTITY_TYPE, name);
		return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
	}
}
