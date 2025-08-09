package io.github.slimeistdev.volare.infrastructure;

import net.minecraft.entity.Entity;
import net.minecraft.entity.PositionInterpolator;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class QuatPositionInterpolator extends PositionInterpolator {
	private final QuatData quatData = new QuatData(0, new Quaternionf());
	@Nullable
	private Quaternionf lastQuat;
	private final QuatEntity quatEntity;

	public <T extends Entity & QuatEntity> QuatPositionInterpolator(T entity) {
		super(entity);
		this.quatEntity = entity;
	}

	public <T extends Entity & QuatEntity> QuatPositionInterpolator(T entity, int lerpDuration) {
		super(entity, lerpDuration);
		this.quatEntity = entity;
	}

	public Quaternionfc getLerpedQuat() {
		return this.quatData.step > 0 ? this.quatData.quat : this.quatEntity.getQuatClient();
	}

	public void refreshPositionAndAngles$Quat(Quaternionf quat) {
		this.quatData.step = this.lerpDuration;

		this.quatData.quat = new Quaternionf(quat);
		this.lastQuat = new Quaternionf(this.quatEntity.getQuatClient());
	}

	public boolean isInterpolatingQuat() {
		return this.quatData.step > 0;
	}

	@Override
	public void tick() {
		if (!this.isInterpolatingQuat()) {
			this.clearQuat();
		} else {
			double d = 1.0 /  this.quatData.step;

			var scratch = new Quaternionf();

			if (this.lastQuat != null) {
				Quaternionf deltaQuat = this.quatEntity.getQuatClient().mul(this.lastQuat.invert(scratch), scratch);
				this.quatData.addQuat(deltaQuat);
			}

			Quaternionf lerpedQuat = this.quatEntity.getQuatClient().slerp(this.quatData.quat, (float) d, scratch);
			this.quatEntity.setQuatClient(lerpedQuat);

			this.quatData.tick();

			this.lastQuat = new Quaternionf(this.quatEntity.getQuatClient());
		}

		super.tick();
	}

	public void clearQuat() {
		this.quatData.step = 0;
		this.lastQuat = null;
	}

	static class QuatData {
		int step;
		Quaternionf quat;

		QuatData(int step, Quaternionf quat) {
			this.step = step;
			this.quat = quat;
		}

		public void tick() {
			step--;
		}

		public void addQuat(Quaternionfc deltaQuat) {
			this.quat = this.quat.mul(deltaQuat).normalize();
		}
	}
}
