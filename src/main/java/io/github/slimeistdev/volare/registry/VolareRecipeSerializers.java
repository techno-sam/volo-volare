package io.github.slimeistdev.volare.registry;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.crafting.GliderParticleRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@SuppressWarnings("SameParameterValue")
public class VolareRecipeSerializers {
	public static final RecipeSerializer<GliderParticleRecipe> GLIDER_PARTICLE = register(
		"glider_particle",
		new GliderParticleRecipe.Serializer()
	);

	private static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
		return Registry.register(Registries.RECIPE_SERIALIZER, Volare.id(id), serializer);
	}

	public static void init() {}
}
