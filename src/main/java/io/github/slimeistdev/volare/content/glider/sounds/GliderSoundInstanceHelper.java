package io.github.slimeistdev.volare.content.glider.sounds;

import io.github.slimeistdev.volare.content.glider.GliderEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

class GliderSoundInstanceHelper {
	static void tryCreate(PlayerEntity player, GliderEntity glider) {
		if (player instanceof ClientPlayerEntity clientPlayer) {
			MinecraftClient.getInstance().getSoundManager().play(new GliderSoundInstance(clientPlayer, glider));
		}
	}
}
