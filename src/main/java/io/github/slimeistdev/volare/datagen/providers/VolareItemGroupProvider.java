package io.github.slimeistdev.volare.datagen.providers;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.components.GliderParticlesComponent;
import io.github.slimeistdev.volare.infrastructure.ItemGroupData;
import io.github.slimeistdev.volare.registry.VolareDataComponentTypes;
import io.github.slimeistdev.volare.registry.VolareItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class VolareItemGroupProvider extends FabricCodecDataProvider<ItemGroupData> {
	public VolareItemGroupProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
		super(output, registries, DataOutput.OutputType.RESOURCE_PACK, "volare_item_groups", ItemGroupData.CODEC);
	}

	@Override
	protected void configure(BiConsumer<Identifier, ItemGroupData> provider, RegistryWrapper.WrapperLookup lookup) {
		var main = ItemGroupData.builder();
		main.add(VolareItems.GLIDER);

		ItemStack gliderWithParticles = VolareItems.GLIDER.getDefaultStack();
		gliderWithParticles.set(VolareDataComponentTypes.GLIDER_PARTICLES, new GliderParticlesComponent(List.of(
			ParticleTypes.CHERRY_LEAVES,
			ParticleTypes.END_ROD,
			ParticleTypes.GLOW
		)));
		main.add(gliderWithParticles);

		provider.accept(Volare.id("main"), main.build());
	}

	@Override
	public String getName() {
		return "Volare Item Groups";
	}
}
