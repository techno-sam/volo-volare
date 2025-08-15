package io.github.slimeistdev.volare.registry;

import com.mojang.brigadier.CommandDispatcher;
import io.github.slimeistdev.volare.compat.area_lib.AreaLibProxy;
import io.github.slimeistdev.volare.registry.commands.ReloadCommandsCommand;
import io.github.slimeistdev.volare.util.Utils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class VolareCommands {
	public static void init() {
		CommandRegistrationCallback.EVENT.register(VolareCommands::register);
	}

	private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
		var volareCommand = literal("volare");

		if (Utils.isDevEnv()) {
			volareCommand.then(ReloadCommandsCommand.register(dispatcher, registryAccess, env, VolareCommands::register));
		}

		AreaLibProxy.registerCommands(volareCommand);

		var built = volareCommand.build();
		if (!built.getChildren().isEmpty()) {
			dispatcher.getRoot().addChild(built);
		}
	}
}
