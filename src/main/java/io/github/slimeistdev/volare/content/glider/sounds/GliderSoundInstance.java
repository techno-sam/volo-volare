package io.github.slimeistdev.volare.content.glider.sounds;

import io.github.slimeistdev.volare.content.glider.GliderEntity;
import io.github.slimeistdev.volare.registry.VolareSoundEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
class GliderSoundInstance extends MovingSoundInstance {
	private static final int DELAY = 20;
	private final ClientPlayerEntity player;
	private final GliderEntity glider;
	private int tickCount;

	public GliderSoundInstance(ClientPlayerEntity player, GliderEntity glider) {
		super(VolareSoundEvents.GLIDER_FLYING, SoundCategory.NEUTRAL, SoundInstance.createRandom());
		this.player = player;
		this.glider = glider;

		this.repeat = true;
		this.repeatDelay = 0;
		this.volume = 0.1f;
	}

	@Override
	public void tick() {
		this.tickCount++;

		if (!this.player.isRemoved() && !this.glider.isRemoved() && (this.tickCount <= DELAY || this.player.getRootVehicle() == this.glider)) {
			this.x = this.player.getX();
			this.y = this.player.getY();
			this.z = this.player.getZ();
			float speed = this.glider.getClientSpeed();
			if (speed >= 1e-7) {
				this.volume = MathHelper.clamp(speed / 4.0f, 0.0f, 1.0f);
			} else {
				this.volume = 0.0f;
			}

			if (this.tickCount < DELAY) {
				this.volume = 0.0f;
			} else if (this.tickCount < DELAY * 2) {
				this.volume = this.volume * ((this.tickCount - DELAY) / (float) DELAY);
			}

			float threshold = 0.8f;
			if (this.volume > threshold) {
				this.pitch = 1.0f + (this.volume - threshold);
			} else {
				this.pitch = 1.0f;
			}
		} else {
			this.setDone();
		}
	}
}
