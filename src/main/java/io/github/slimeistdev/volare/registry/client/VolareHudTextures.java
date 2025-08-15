package io.github.slimeistdev.volare.registry.client;

import io.github.slimeistdev.volare.Volare;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.Locale;

@Environment(EnvType.CLIENT)
public enum VolareHudTextures {
	ROLL_INDICATOR(31, 31)
	;

	public final Identifier id;
	public final int width;
	public final int height;

	VolareHudTextures(int width, int height) {
		this.id = Volare.id("hud/" + this.name().toLowerCase(Locale.ROOT));
		this.width = width;
		this.height = height;
	}
}
