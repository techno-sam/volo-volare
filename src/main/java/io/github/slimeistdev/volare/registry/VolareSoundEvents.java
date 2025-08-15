package io.github.slimeistdev.volare.registry;

import io.github.slimeistdev.volare.Volare;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

@SuppressWarnings("SameParameterValue")
public class VolareSoundEvents {
	public static final SoundEvent GLIDER_BOOST = register("entity.glider.boost");

	private static SoundEvent register(String id) {
		var id_ = Volare.id(id);
		return Registry.register(Registries.SOUND_EVENT, id_, SoundEvent.of(id_));
	}

	public static void init() {}
}
