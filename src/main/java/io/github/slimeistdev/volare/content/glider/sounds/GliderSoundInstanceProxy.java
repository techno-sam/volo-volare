package io.github.slimeistdev.volare.content.glider.sounds;

import io.github.slimeistdev.volare.content.glider.GliderEntity;
import io.github.slimeistdev.volare.util.Env;
import net.minecraft.entity.player.PlayerEntity;

public class GliderSoundInstanceProxy {
	public static void tryCreate(PlayerEntity player, GliderEntity glider) {
		if (player.getWorld().isClient) {
			Env.CLIENT.runIfCurrent(() -> () -> {
				GliderSoundInstanceHelper.tryCreate(player, glider);
			});
		}
	}
}
