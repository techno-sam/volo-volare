package io.github.slimeistdev.volare.registry;

import io.github.slimeistdev.volare.Volare;
import net.minecraft.block.Block;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

@SuppressWarnings("SameParameterValue")
public class VolareTags {
	public static final TagKey<Block> THERMAL_SOURCE = block("thermal_source");
	public static final TagKey<Item> THRUST_SOURCE = item("thrust_source");
	public static final TagKey<DamageType> EXPLODE_GLIDER = damageType("explode_glider");

	private static TagKey<Block> block(String path) {
		return TagKey.of(RegistryKeys.BLOCK, Volare.id(path));
	}

	private static TagKey<Item> item(String path) {
		return TagKey.of(RegistryKeys.ITEM, Volare.id(path));
	}

	private static TagKey<DamageType> damageType(String path) {
		return TagKey.of(RegistryKeys.DAMAGE_TYPE, Volare.id(path));
	}
}
