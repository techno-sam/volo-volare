package io.github.slimeistdev.volare.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.IntSlider;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import io.github.slimeistdev.volare.Volare;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.ApiStatus;

public class VolareClientConfig {
	private static final ConfigClassHandler<VolareClientConfig> HANDLER = ConfigClassHandler.createBuilder(VolareClientConfig.class)
		.id(Volare.id("client_config"))
		.serializer(config -> GsonConfigSerializerBuilder.create(config)
			.setPath(FabricLoader.getInstance().getConfigDir().resolve("volare.json5"))
			.setJson5(true)
			.build())
		.build();

	@SerialEntry
	@AutoGen(category = "general")
	@IntSlider(min = 0, max = 42, step = 1)
	public int placeholder = 0;

	@ApiStatus.Internal
	public VolareClientConfig() {}

	public static VolareClientConfig get() {
		return HANDLER.instance();
	}

	public static boolean load() {
		return HANDLER.load();
	}

	@Environment(EnvType.CLIENT)
	public static Screen createScreen(Screen parent) {
		return HANDLER.generateGui()
			.generateScreen(parent);
	}
}
