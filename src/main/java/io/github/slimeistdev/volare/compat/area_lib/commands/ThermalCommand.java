package io.github.slimeistdev.volare.compat.area_lib.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import io.github.slimeistdev.volare.compat.area_lib.AreaLibComponents;
import io.github.slimeistdev.volare.compat.area_lib.ThermalComponent;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ThermalCommand {
	public static ArgumentBuilder<ServerCommandSource, ?> register() {
		return literal("thermal")
			.requires(cs -> cs.hasPermissionLevel(2))
			.then(argument("area", AreaArgument.area())
				.then(literal("remove")
					.executes(ctx -> remove(
						ctx.getSource(),
						AreaArgument.getArea(ctx, "area")
					))
				)
				.executes(ctx -> set(
					ctx.getSource(),
					AreaArgument.getArea(ctx, "area")
				))
				.then(argument("strength", FloatArgumentType.floatArg(-10.0f, 10.0f))
					.executes(ctx -> set(
						ctx.getSource(),
						AreaArgument.getArea(ctx, "area"),
						FloatArgumentType.getFloat(ctx, "strength")
					))
					.then(literal("relative")
						.executes(ctx -> set(
							ctx.getSource(),
							AreaArgument.getArea(ctx, "area"),
							FloatArgumentType.getFloat(ctx, "strength"),
							false
						))
					)
				)
			);
	}

	private static int set(ServerCommandSource source, Area area) {
		return set(source, area, 1.0f);
	}

	private static int set(ServerCommandSource source, Area area, float strength) {
		return set(source, area, strength, true);
	}

	private static int set(ServerCommandSource source, Area area, float strength, boolean absolute) {
		var server = source.getServer();

		area.put(server, AreaLibComponents.THERMAL, new ThermalComponent(strength, absolute));
		if (absolute) {
			source.sendFeedback(() -> Text.translatable("commands.volare.area.thermal.set.absolute", area.getId().toString(), strength), true);
		} else {
			source.sendFeedback(() -> Text.translatable("commands.volare.area.thermal.set.relative", area.getId().toString(), strength), true);
		}

		return 1;
	}

	private static int remove(ServerCommandSource source, Area area) {
		var server = source.getServer();

		area.remove(server, AreaLibComponents.THERMAL);
		source.sendFeedback(() -> Text.translatable("commands.volare.area.thermal.removed", area.getId().toString()), true);
		return 1;
	}
}
