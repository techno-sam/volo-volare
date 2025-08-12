package io.github.slimeistdev.volare.registry;

import io.github.slimeistdev.volare.Volare;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class VolareTags {
	public static final TagKey<Block> THERMAL_SOURCE = block("thermal_source");

	@SuppressWarnings("SameParameterValue")
	private static TagKey<Block> block(String path) {
		return TagKey.of(RegistryKeys.BLOCK, Volare.id(path));
	}
}
