package io.github.slimeistdev.volare.datagen.providers;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.components.GliderParticlesComponent;
import io.github.slimeistdev.volare.content.glider.components.GliderVariant;
import io.github.slimeistdev.volare.infrastructure.ItemGroupData;
import io.github.slimeistdev.volare.registry.VolareDataComponentTypes;
import io.github.slimeistdev.volare.registry.VolareItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class VolareItemGroupProvider extends FabricCodecDataProvider<ItemGroupData> {
	public VolareItemGroupProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
		super(output, registries, DataOutput.OutputType.RESOURCE_PACK, "volare_item_groups", ItemGroupData.CODEC);
	}

	private static final GliderParticlesComponent PARTICLES = new GliderParticlesComponent(List.of(
		ParticleTypes.CHERRY_LEAVES,
		ParticleTypes.END_ROD,
		ParticleTypes.GLOW
	));

	@Override
	protected void configure(BiConsumer<Identifier, ItemGroupData> provider, RegistryWrapper.WrapperLookup lookup) {
		var main = ItemGroupData.builder();

		String[] variants = new String[] {"colorful", "simplified"};

		for (boolean particles : new boolean[] {false, true}) {
			for (String variant : variants) {
				ItemStack stack = VolareItems.GLIDER.getDefaultStack();
				stack.set(
					VolareDataComponentTypes.GLIDER_VARIANT,
					new GliderVariant(new AssetInfo(Volare.id("entity/glider/" + variant)))
				);

				if (particles) {
					stack.set(VolareDataComponentTypes.GLIDER_PARTICLES, PARTICLES);
				}

				main.add(stack);
			}
		}

		provider.accept(Volare.id("main"), main.build());
	}

	@Override
	public String getName() {
		return "Volare Item Groups";
	}
}
