package io.github.slimeistdev.volare.compat.area_lib;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
public class AreaLibProxy {
	public static boolean isLoaded() {
		return FabricLoader.getInstance().isModLoaded("area_lib");
	}

	public static <T> @Nullable T runIfLoaded(Supplier<Supplier<T>> supplier) {
		return runIfLoaded(supplier, null);
	}

	public static <T> T runIfLoaded(Supplier<Supplier<T>> supplier, T defaultValue) {
		if (isLoaded()) {
			return supplier.get().get();
		}
		return defaultValue;
	}

	public static void executeIfLoaded(Supplier<Runnable> runnable) {
		if (isLoaded()) {
			runnable.get().run();
		}
	}

	public static void init() {
		executeIfLoaded(() -> () -> AreaLibPlugin.init());
	}

	public static boolean isInFlak(World world, Entity entity) {
		return runIfLoaded(() -> () -> AreaLibPlugin.isInFlak(world, entity), false);
	}

	public static @Nullable Float getThermalStrength(World world, Entity entity, float configStrength) {
		return runIfLoaded(() -> () -> AreaLibPlugin.getThermalStrength(world, entity, configStrength), null);
	}

	public static void registerCommands(LiteralArgumentBuilder<ServerCommandSource> builder) {
		executeIfLoaded(() -> () -> AreaLibPlugin.registerCommands(builder));
	}
}
