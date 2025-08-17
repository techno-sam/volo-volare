package io.github.slimeistdev.volare.content.glider.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.slimeistdev.volare.content.glider.components.GliderParticlesComponent;
import io.github.slimeistdev.volare.registry.VolareDataComponentTypes;
import io.github.slimeistdev.volare.registry.VolareRecipeSerializers;
import io.github.slimeistdev.volare.registry.VolareSlotDisplays;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.display.DisplayedItemFactory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SmithingRecipeDisplay;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GliderParticleRecipe implements SmithingRecipe {
	private final String group;
	private final Ingredient base;
	private final Ingredient addition;
	private final ParticleEffect particle;

	@Nullable
	private IngredientPlacement ingredientPlacement;

	public GliderParticleRecipe(String group, Ingredient base, Ingredient addition, ParticleEffect particle) {
		this.group = group;
		this.base = base;
		this.addition = addition;
		this.particle = particle;
	}

	@Override
	public Optional<Ingredient> template() {
		return Optional.empty();
	}

	@Override
	public Ingredient base() {
		return base;
	}

	@Override
	public Optional<Ingredient> addition() {
		return Optional.of(addition);
	}

	@Override
	public boolean matches(SmithingRecipeInput smithingRecipeInput, World world) {
		if (!SmithingRecipe.super.matches(smithingRecipeInput, world))
			return false;

		GliderParticlesComponent particles = smithingRecipeInput.base().get(VolareDataComponentTypes.GLIDER_PARTICLES);
		return particles == null || !particles.hasParticle(particle);
	}

	@Override
	public ItemStack craft(SmithingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
		return craft(input.base(), particle);
	}

	private static ItemStack craft(ItemStack base, ParticleEffect particle) {
		ItemStack output = base.copyWithCount(1);

		GliderParticlesComponent particles = base.getOrDefault(
			VolareDataComponentTypes.GLIDER_PARTICLES,
			new GliderParticlesComponent()
		);

		if (particles.hasParticle(particle)) {
			return ItemStack.EMPTY;
		}

		output.set(
			VolareDataComponentTypes.GLIDER_PARTICLES,
			particles.addParticle(particle)
		);

		return output;
	}

	@Override
	public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
		return VolareRecipeSerializers.GLIDER_PARTICLE;
	}

	@Override
	public IngredientPlacement getIngredientPlacement() {
		if (ingredientPlacement == null) {
			ingredientPlacement = IngredientPlacement.forMultipleSlots(List.of(template(), Optional.of(base), Optional.of(addition)));
		}
		return ingredientPlacement;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public List<RecipeDisplay> getDisplays() {
		SlotDisplay baseDisplay = base.toDisplay();
		SlotDisplay additionDisplay = addition.toDisplay();

		return List.of(
			new SmithingRecipeDisplay(
				SlotDisplay.EmptySlotDisplay.INSTANCE,
				baseDisplay,
				additionDisplay,
				new OutputSlotDisplay(baseDisplay, particle),
				new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
			)
		);
	}

	public record OutputSlotDisplay(SlotDisplay base, ParticleEffect particle) implements SlotDisplay {
		public static final MapCodec<OutputSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			SlotDisplay.CODEC.fieldOf("base").forGetter(OutputSlotDisplay::base),
			ParticleTypes.TYPE_CODEC.fieldOf("particle").forGetter(OutputSlotDisplay::particle)
		).apply(i, OutputSlotDisplay::new));

		public static final PacketCodec<RegistryByteBuf, OutputSlotDisplay> PACKET_CODEC = PacketCodec.tuple(
			SlotDisplay.PACKET_CODEC,
			r -> r.base,
			ParticleTypes.PACKET_CODEC,
			r -> r.particle,
			OutputSlotDisplay::new
		);

		public static final SlotDisplay.Serializer<OutputSlotDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

		@Override
		public Serializer<? extends SlotDisplay> serializer() {
			return VolareSlotDisplays.GLIDER_PARTICLE_OUTPUT;
		}

		@Override
		public boolean isEnabled(FeatureSet features) {
			return base.isEnabled(features);
		}

		@Override
		public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
			if (factory instanceof DisplayedItemFactory.FromStack<T> fromStack) {
				return base.getStacks(parameters)
					.stream()
					.map(stack -> fromStack.toDisplayed(craft(stack, particle)));
			}

			return Stream.empty();
		}
	}

	public static class Serializer implements RecipeSerializer<GliderParticleRecipe> {
		private static final MapCodec<GliderParticleRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
			Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
			Ingredient.CODEC.fieldOf("addition").forGetter(r -> r.addition),
			ParticleTypes.TYPE_CODEC.fieldOf("particle").forGetter(r -> r.particle)
		).apply(i, GliderParticleRecipe::new));

		public static final PacketCodec<RegistryByteBuf, GliderParticleRecipe> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.STRING,
			r -> r.group,
			Ingredient.PACKET_CODEC,
			r -> r.base,
			Ingredient.PACKET_CODEC,
			r -> r.addition,
			ParticleTypes.PACKET_CODEC,
			r -> r.particle,
			GliderParticleRecipe::new
		);

		@Override
		public MapCodec<GliderParticleRecipe> codec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, GliderParticleRecipe> packetCodec() {
			return PACKET_CODEC;
		}
	}
}
