package io.github.slimeistdev.volare.compat.area_lib;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.doublekekse.area_lib.AreaLib;
import io.github.slimeistdev.volare.compat.area_lib.commands.FlakCommand;
import io.github.slimeistdev.volare.compat.area_lib.commands.ThermalCommand;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.server.command.CommandManager.literal;

class AreaLibPlugin {
	public static void init() {
		AreaLibComponents.init();
	}

	public static boolean isInFlak(World world, Entity entity) {
		for (var area : AreaLib.getSavedData(world).findTrackedAreasContaining(entity)) {
			if (area.has(AreaLibComponents.FLAK)) {
				return true;
			}
		}
		return false;
	}

	public static @Nullable Float getThermalStrength(World world, Entity entity, float configStrength) {
		for (var area : AreaLib.getSavedData(world).findTrackedAreasContaining(entity)) {
			if (area.has(AreaLibComponents.THERMAL)) {
				return area.get(AreaLibComponents.THERMAL).calculateStrength(configStrength);
			}
		}

		return null;
	}

	public static void registerCommands(LiteralArgumentBuilder<ServerCommandSource> builder) {
		builder.then(literal("area")
			.then(FlakCommand.register())
			.then(ThermalCommand.register())
		);
	}
}
