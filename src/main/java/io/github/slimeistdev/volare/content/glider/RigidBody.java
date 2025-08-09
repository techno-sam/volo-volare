package io.github.slimeistdev.volare.content.glider;

import org.joml.*;

public class RigidBody {
	public final float mass;
	/** inertia tensor, local space */
	public final Matrix3fc inertia;
	/** pre-inverted inertia tensor, local space */
	public final Matrix3fc inverseInertia;

	/** global space */
	private final Quaternionf orientation;

	/** global space, blocks / second */
	private final Vector3f velocity;
	/** local space, radians / second */
	private final Vector3f angularVelocity;

	/** global space */
	private final Vector3f force = new Vector3f(0);
	/** local space */
	private final Vector3f torque = new Vector3f(0);

	public RigidBody(RigidBodyParams params) {
		this(params, new Quaternionf(), new Vector3f(0), new Vector3f(0));
	}

	public RigidBody(RigidBodyParams params, Quaternionf orientation, Vector3f velocity, Vector3f angularVelocity) {
		this.mass = params.mass();
		this.inertia = params.inertia();
		this.inverseInertia = params.inertia().invert(new Matrix3f());

		this.orientation = orientation;

		this.velocity = velocity;
		this.angularVelocity = angularVelocity;
	}

	/** set velocity, in global space. blocks/sec */
	public void setVelocity(Vector3fc velocity) {
		this.velocity.set(velocity);
	}

	/** get velocity, in global space. blocks/sec */
	public Vector3fc getVelocity() {
		return velocity;
	}

	/** get angular velocity, in local space. radians/sec */
	public Vector3fc getAngularVelocity() {
		return angularVelocity;
	}

	/** set orientation, in global space */
	public void setOrientation(Quaternionfc orientation) {
		this.orientation.set(orientation);
	}

	/** get orientation, in global space */
	public Quaternionfc getOrientation() {
		return orientation;
	}

	// region transform

	/** Transform direction from local to global space */
	Vector3f directionToGlobal(Vector3fc local) {
		return directionToGlobal(local, new Vector3f());
	}

	/** Transform direction from local to global space */
	Vector3f directionToGlobal(Vector3fc local, Vector3f dest) {
		return orientation.transform(local, dest);
	}

	/** Transform direction from global to local space */
	Vector3f directionToLocal(Vector3fc global) {
		return directionToLocal(global, new Vector3f());
	}

	/** Transform direction from global to local space */
	Vector3f directionToLocal(Vector3fc global, Vector3f dest) {
		return orientation.transformInverse(global, dest);
	}

	/** Get motion at point due to linear & angular velocity, all in local space */
	Vector3f velocityAtPoint(Vector3fc localPoint) {
		return velocityAtPoint(localPoint, new Vector3f());
	}

	/** Get motion at point due to linear & angular velocity, all in local space */
	Vector3f velocityAtPoint(Vector3fc localPoint, Vector3f dest) {
		directionToLocal(velocity, dest); // linear component
		dest.add(angularVelocity.cross(localPoint, new Vector3f())); // angular component
		return dest;
	}

	// endregion transform

	/** Apply a force at a point, affecting force and torque vectors. All in local space */
	void applyForceAtPoint(Vector3fc localForce, Vector3fc localPoint) {
		Vector3f scratch = new Vector3f();

		force.add(directionToGlobal(localForce, scratch));
		torque.add(localPoint.cross(localForce, scratch));
	}

	/** Apply local force at center of mass */
	void applyForceAtCoM(Vector3fc localForce) {
		force.add(directionToGlobal(localForce, new Vector3f()));
	}

	// https://physics.stackexchange.com/questions/790061/calculate-rotation-from-net-torque-and-inertia-matrix
	public void endStep(float dt) {
		Vector3f scratch = new Vector3f();

		Vector3f acceleration = force.div(mass, scratch);
		acceleration.mul(dt);
		velocity.add(acceleration);

		// angular acceleration.
		Vector3f angularMomentum = inertia.transform(angularVelocity, new Vector3f());
		// angularAcceleration = inverseInertia * (torque - (angularVelocity x angularMomentum))
		Vector3f angularAcceleration = inverseInertia.transform(torque.sub(angularVelocity.cross(angularMomentum, scratch), scratch));
		angularAcceleration.mul(dt);
		angularVelocity.add(angularAcceleration);

		orientation.add(
			orientation.mul(
				new Quaternionf(angularVelocity.x, angularVelocity.y, angularVelocity.z, 0.0f).scale(0.5f * dt),
				new Quaternionf()
			)
		);
		orientation.normalize();

		force.set(0);
		torque.set(0);
	}
}
