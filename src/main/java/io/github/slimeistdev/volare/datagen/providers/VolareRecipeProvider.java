package io.github.slimeistdev.volare.datagen.providers;

import io.github.slimeistdev.volare.content.glider.crafting.GliderParticleRecipeJsonBuilder;
import io.github.slimeistdev.volare.registry.VolareItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

import java.util.concurrent.CompletableFuture;

public class VolareRecipeProvider extends FabricRecipeProvider {
	public VolareRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
		return new Generator(registries, exporter);
	}

	@Override
	public String getName() {
		return "Volare Recipes";
	}

	@SuppressWarnings("SameParameterValue")
	private static class Generator extends RecipeGenerator {
		protected Generator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
			super(registries, exporter);
		}

		protected void offerGliderParticles(Item addition, ParticleEffect particleEffect) {
			this.offerGliderParticles(Ingredient.ofItem(addition), particleEffect, getItemPath(addition));
		}

		protected void offerGliderParticles(TagKey<Item> addition, ParticleEffect particleEffect) {
			this.offerGliderParticles(ingredientFromTag(addition), particleEffect, "tag_"+addition.id().toUnderscoreSeparatedString());
		}

		protected void offerGliderParticles(Ingredient addition, ParticleEffect particle, String path) {
			this.offerGliderParticles(addition, particle, path, "");
		}

		protected void offerGliderParticles(Ingredient addition, ParticleEffect particle, String path, String group) {
			new GliderParticleRecipeJsonBuilder(
				Ingredient.ofItem(VolareItems.GLIDER),
				addition,
				particle,
				RecipeCategory.TRANSPORTATION
			)
				.criterion("has_glider", this.conditionsFromItem(VolareItems.GLIDER))
				.group(group)
				.offerTo(exporter, "glider_particles_" + path);
		}

		@Override
		public void generate() {
			offerGliderParticles(Items.CHERRY_LEAVES, ParticleTypes.CHERRY_LEAVES);
			offerGliderParticles(Items.DRAGON_BREATH, ParticleTypes.DRAGON_BREATH);
			offerGliderParticles(Items.END_ROD, ParticleTypes.END_ROD);
			offerGliderParticles(Items.LIGHTNING_ROD, ParticleTypes.ELECTRIC_SPARK);
			offerGliderParticles(Items.BOOKSHELF, ParticleTypes.ENCHANT);
			offerGliderParticles(Items.FIREFLY_BUSH, ParticleTypes.FIREFLY);
			offerGliderParticles(Items.TORCH, ParticleTypes.FLAME);
			offerGliderParticles(Items.GLOW_INK_SAC, ParticleTypes.GLOW);
			offerGliderParticles(Items.RED_DYE, ParticleTypes.HEART);
			offerGliderParticles(Items.PALE_OAK_LEAVES, ParticleTypes.PALE_OAK_LEAVES);
			offerGliderParticles(ItemTags.CANDLES, ParticleTypes.SMALL_FLAME);
			offerGliderParticles(ItemTags.SOUL_FIRE_BASE_BLOCKS, ParticleTypes.SOUL);
			offerGliderParticles(Items.SOUL_TORCH, ParticleTypes.SOUL_FIRE_FLAME);
			offerGliderParticles(Items.SPORE_BLOSSOM, ParticleTypes.SPORE_BLOSSOM_AIR);
			offerGliderParticles(Items.TRIAL_KEY, ParticleTypes.TRIAL_OMEN);
		}
	}
}
