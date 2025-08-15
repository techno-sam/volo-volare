package io.github.slimeistdev.volare.registry.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class ReloadCommandsCommand {
	public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env, CommandRegistrationCallback callback) {
		return literal("reload_commands")
			.requires(cs -> cs.hasPermissionLevel(2))
			.executes(ctx -> {
				callback.register(dispatcher, registryAccess, env);
				for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
					ctx.getSource().getServer().getCommandManager().sendCommandTree(player);
				}
				ctx.getSource().sendFeedback(() -> Text.literal("Reloaded commands!"), true);
				return 1;
			});
	}
}
