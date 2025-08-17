package io.github.slimeistdev.volare.registry;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.crafting.GliderParticleRecipe;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@SuppressWarnings("SameParameterValue")
public class VolareSlotDisplays {
	public static final SlotDisplay.Serializer<GliderParticleRecipe.OutputSlotDisplay> GLIDER_PARTICLE_OUTPUT = register(
		"glider_particle_output",
		GliderParticleRecipe.OutputSlotDisplay.SERIALIZER
	);

	private static <T extends SlotDisplay> SlotDisplay.Serializer<T> register(String id, SlotDisplay.Serializer<T> serializer) {
		return Registry.register(Registries.SLOT_DISPLAY, Volare.id(id), serializer);
	}

	public static void init() {}
}
