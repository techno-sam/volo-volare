package io.github.slimeistdev.volare.content.glider;

import org.joml.Vector3f;

public record GliderWings(Wing leftAileron, Wing rightAileron, Wing elevator, Wing rudder) {
	/**
	 * Apply lift and drag forces to the given rigid body.
	 * @param body the rigid body to apply forces to
	 * @param dt time step
	 * @param altitude height in meters above sea level
	 * @return whether any forces were applied
	 */
	public boolean applyForcesTo(RigidBody body, float dt, float altitude) {
		boolean any = false;
		any |= leftAileron.applyForcesTo(body, dt, altitude);
		any |= rightAileron.applyForcesTo(body, dt, altitude);
		any |= elevator.applyForcesTo(body, dt, altitude);
		any |= rudder.applyForcesTo(body, dt, altitude);
		return any;
	}

	public void applyControls(float pitchControl, float rollControl) {
		// coordinate rudder and ailerons
		float rudderControl = rollControl;//* 0.5f;
		rollControl = 0;

		leftAileron.setControlInput(-rollControl);
		rightAileron.setControlInput(rollControl);
		elevator.setControlInput(-pitchControl);
		rudder.setControlInput(-rudderControl);
	}

	/**
	 * Calculate the center of pressure.
	 * @param alpha angle of attack in degrees
	 * @return the center of pressure in local space
	 */
	public Vector3f calculateCenterOfPressure(float alpha) {
		return calculateCenterOfPressure(alpha, new Vector3f());
	}

	/**
	 * Calculate the (CoM-relative) center of pressure.
	 * @param dest destination vector to store the result
	 * @param alpha angle of attack in degrees
	 * @return the center of pressure in local space
	 */
	public Vector3f calculateCenterOfPressure(float alpha, Vector3f dest) {
		dest.set(0, 0, 0);
		float totalForce = 0.0f;

		Wing[] liftingWings = {leftAileron, rightAileron, elevator};
		for (Wing wing : liftingWings) {
			totalForce += wing.contributeToCenterOfPressure(dest, alpha);
		}

		if (totalForce > 0.0f) {
			dest.div(totalForce);
		} else {
			dest.set(0, 0, 0);
		}

		return dest;
	}
}
