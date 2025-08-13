package io.github.slimeistdev.volare.config;

import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.FloatSlider;
import dev.isxander.yacl3.config.v2.api.autogen.IntSlider;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.config.serializers.NoOpConfigSerializer;
import io.github.slimeistdev.volare.config.serializers.SaveCallbackConfigSerializer;
import io.github.slimeistdev.volare.mixin_interfaces.YetAnotherConfigLibImplDuck;
import io.github.slimeistdev.volare.network.VolarePackets;
import io.github.slimeistdev.volare.network.s2c.SetConfigS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class VolareServerConfig {
	private static final ConfigClassHandler<VolareServerConfig> CLIENT_HANDLER = ConfigClassHandler.createBuilder(VolareServerConfig.class)
		.id(Volare.id("server_config"))
		.serializer(NoOpConfigSerializer::new)
		.build();

	@Nullable
	private static ConfigClassHandler<VolareServerConfig> SERVER_HANDLER = null;

	@Nullable
	private static WeakReference<MinecraftServer> CURRENT_SERVER = null;

	@SerialEntry(comment = "Vertical extent of thermals in blocks. A value of 0 disables thermals.")
	@AutoGen(category = "thermals")
	@IntSlider(min = 0, max = 128, step = 1)
	public int thermalsHeight = 32;

	@SerialEntry(comment = "Thermal strength is constant until this height, then follows inverse-squared falloff.")
	@AutoGen(category = "thermals")
	@IntSlider(min = 0, max = 128, step = 1)
	public int thermalsFullStrengthHeight = 8;

	@SerialEntry(comment = "Thermal strength in blocks per second. A value of 0 disables thermals.")
	@AutoGen(category = "thermals")
	@FloatSlider(min = 0.0f, max = 10.0f, step = 0.1f)
	public float thermalsStrength = 2.5f;

	@ApiStatus.Internal
	public VolareServerConfig() {}

	public static VolareServerConfig get(World world) {
		return getHandler(world).instance();
	}

	public static ConfigClassHandler<VolareServerConfig> getHandler(World world) {
		if (world instanceof ServerWorld serverWorld) {
			return getOrCreateServerHandler(serverWorld.getServer());
		} else if (world == null || world.isClient) {
			return CLIENT_HANDLER;
		} else {
			throw new IllegalArgumentException("World is neither client nor server world: " + world);
		}
	}

	private static ConfigClassHandler<VolareServerConfig> getOrCreateServerHandler(@NotNull MinecraftServer currentServer) {
		boolean replace = SERVER_HANDLER == null || CURRENT_SERVER == null || CURRENT_SERVER.get() != currentServer;

		if (replace) {
			if (SERVER_HANDLER != null) {
				SERVER_HANDLER.save();
			}

			CURRENT_SERVER = new WeakReference<>(currentServer);

			SERVER_HANDLER = ConfigClassHandler.createBuilder(VolareServerConfig.class)
				.id(Volare.id("server_config"))
				.serializer(config -> new SaveCallbackConfigSerializer<>(
					config,
					GsonConfigSerializerBuilder.create(config).
						setPath(currentServer.getSavePath(WorldSavePath.ROOT).resolve("serverconfig").resolve("volare-server.json5"))
						.setJson5(true)
						.build(),
					cfg -> currentServer.getPlayerManager().sendToAll(VolarePackets.PACKETS.tunnelPacket(new SetConfigS2CPacket(cfg)))
				))
				.build();

			SERVER_HANDLER.load();
		}

		return SERVER_HANDLER;
	}

	public VolareServerConfig copy() {
		VolareServerConfig copy = new VolareServerConfig();

		for (var field : VolareServerConfig.class.getDeclaredFields()) {
			if (field.isAnnotationPresent(SerialEntry.class)) {
				try {
					field.set(copy, field.get(this));
				} catch (IllegalAccessException e) {
					Volare.LOG.error("Failed to copy field {} in VolareServerConfig", field.getName(), e);
				}
			}
		}

		return copy;
	}

	@Environment(EnvType.CLIENT)
	public static Screen createScreen(Screen parent) {
		YetAnotherConfigLib yacl = CLIENT_HANDLER.generateGui();
		((YetAnotherConfigLibImplDuck) yacl).volare$setServerSided(true);
		((YetAnotherConfigLibImplDuck) yacl).volare$setHandler(CLIENT_HANDLER);
		return yacl.generateScreen(parent);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean isAuthorizedToChange(PlayerEntity player) {
		return player.hasPermissionLevel(2);
	}
}
