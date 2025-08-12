package io.github.slimeistdev.volare.content.glider;

import org.joml.Vector3f;

public record GliderWings(Wing leftAileron, Wing rightAileron, Wing elevator, Wing rudder) {
	/**
	 * Apply lift and drag forces to the given rigid body.
	 * @param body the rigid body to apply forces to
	 * @param dt time step
	 * @param airDensity density in kg/m^3 of the air at the current altitude
	 * @param thermalSpeed airspeed of rising thermals in m/s
	 * @return whether any forces were applied
	 */
	public boolean applyForcesTo(RigidBody body, float dt, float airDensity, float thermalSpeed) {
		boolean any = false;
		any |= leftAileron.applyForcesTo(body, dt, airDensity, thermalSpeed);
		any |= rightAileron.applyForcesTo(body, dt, airDensity, thermalSpeed);
		any |= elevator.applyForcesTo(body, dt, airDensity, thermalSpeed);
		any |= rudder.applyForcesTo(body, dt, airDensity, thermalSpeed);
		return any;
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	public void applyControls(float pitchControl, float yawControl) {
		// coordinate rudder and ailerons
		float aileronControl = yawControl * 0.25f;
		float rudderControl = yawControl;

		leftAileron.setControlInput(-aileronControl);
		rightAileron.setControlInput(aileronControl);
		elevator.setControlInput(pitchControl);
		rudder.setControlInput(rudderControl);
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
