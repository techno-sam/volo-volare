package io.github.slimeistdev.volare.content.glider.crafting;

import io.github.slimeistdev.volare.Volare;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.LinkedHashMap;
import java.util.Map;

public class GliderParticleRecipeJsonBuilder {
	private String group = "";
	private final Ingredient base;
	private final Ingredient addition;
	private final ParticleEffect particle;
	private final RecipeCategory category;
	private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

	public GliderParticleRecipeJsonBuilder(Ingredient base, Ingredient addition, ParticleEffect particle, RecipeCategory category) {
		this.base = base;
		this.addition = addition;
		this.particle = particle;
		this.category = category;
	}

	public GliderParticleRecipeJsonBuilder group(String group) {
		this.group = group;
		return this;
	}

	public GliderParticleRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
		if (this.criteria.put(name, criterion) != null) {
			throw new IllegalArgumentException("Duplicate advancement criterion: " + name);
		}
		return this;
	}

	public void offerTo(RecipeExporter exporter, String recipeId) {
		this.offerTo(exporter, Volare.key(RegistryKeys.RECIPE, recipeId));
	}

	public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
		this.validate(recipeKey);
		Advancement.Builder builder = exporter.getAdvancementBuilder()
			.criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
			.rewards(AdvancementRewards.Builder.recipe(recipeKey))
			.criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
		this.criteria.forEach(builder::criterion);
		GliderParticleRecipe recipe = new GliderParticleRecipe(
			this.group,
			this.base,
			this.addition,
			this.particle
		);
		exporter.accept(recipeKey, recipe, builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
	}

	private void validate(RegistryKey<Recipe<?>> recipeKey) {
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + recipeKey.getValue());
		}
	}
}
