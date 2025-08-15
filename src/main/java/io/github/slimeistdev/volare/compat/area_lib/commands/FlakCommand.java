package io.github.slimeistdev.volare.compat.area_lib.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import io.github.slimeistdev.volare.compat.area_lib.AreaLibComponents;
import io.github.slimeistdev.volare.compat.area_lib.FlakComponent;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FlakCommand {
	public static ArgumentBuilder<ServerCommandSource, ?> register() {
		return literal("flak")
			.requires(cs -> cs.hasPermissionLevel(2))
			.then(argument("area", AreaArgument.area())
				.executes(ctx -> execute(
					ctx.getSource(),
					AreaArgument.getArea(ctx, "area"),
					true
				))
				.then(literal("remove")
					.executes(ctx -> execute(
						ctx.getSource(),
						AreaArgument.getArea(ctx, "area"),
						false
					))
				)
			);
	}

	private static int execute(ServerCommandSource source, Area area, boolean add) {
		var server = source.getServer();

		if (add) {
			area.put(server, AreaLibComponents.FLAK, FlakComponent.INSTANCE);
			source.sendFeedback(() -> Text.translatable("commands.volare.area.flak.added", area.getId().toString()), true);
		} else {
			area.remove(server, AreaLibComponents.FLAK);
			source.sendFeedback(() -> Text.translatable("commands.volare.area.flak.removed", area.getId().toString()), true);
		}

		return 1;
	}
}
