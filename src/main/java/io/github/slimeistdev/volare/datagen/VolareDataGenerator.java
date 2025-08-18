package io.github.slimeistdev.volare.datagen;

import io.github.slimeistdev.volare.datagen.providers.VolareItemGroupProvider;
import io.github.slimeistdev.volare.datagen.providers.VolareRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class VolareDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator gen) {
		FabricDataGenerator.Pack pack = gen.createPack();

		pack.addProvider(VolareItemGroupProvider::new);
		pack.addProvider(VolareRecipeProvider::new);
	}
}
