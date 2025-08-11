package io.github.slimeistdev.volare.util;

import net.minecraft.util.math.MathHelper;

@SuppressWarnings("UnusedReturnValue")
public class LerpedFloat {
	protected float lastValue;
	protected float value;
	protected float target;

	/** delta per tick */
	protected float speed;

	/**
	 * @param speed delta per tick
	 */
	public LerpedFloat(float speed) {
		this.value = 0.0f;
		this.target = 0.0f;
		this.speed = speed;
	}

	public LerpedFloat setTarget(float target) {
		this.target = target;
		return this;
	}

	public LerpedFloat setValue(float value) {
		this.lastValue = this.value;
		this.value = value;
		return this;
	}

	public LerpedFloat snapTo(float f) {
		this.lastValue = this.value;
		this.value = f;
		this.target = f;
		return this;
	}

	public float get() {
		return this.value;
	}

	public float get(float tickProgress) {
		return MathHelper.lerp(tickProgress, this.lastValue, this.value);
	}

	public void tick() {
		this.lastValue = this.value;
		if (this.value < this.target) {
			this.value = Math.min(this.value + this.speed, this.target);
		} else if (this.value > this.target) {
			this.value = Math.max(this.value - this.speed, this.target);
		}
	}
}
