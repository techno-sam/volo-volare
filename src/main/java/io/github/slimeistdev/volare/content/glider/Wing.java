package io.github.slimeistdev.volare.content.glider;

import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Wing {
	private final Vector3fc centerOfPressure;
	private final float span;
	private final float chord;

	private final float area;
	private final float aspectRatio;

	private final Airfoil airfoil;
	private final Vector3fc normal;
	private final Vector3fc spanDir;
	private final float flapRatio;

	private final float flapRatioSqrt;

	private final Vector3fc activeWindComponents;

	@SuppressWarnings("FieldCanBeLocal")
	private final float efficiencyFactor = 1.0f;

	private float controlInput = 0.0f;

	public Wing(Vector3fc centerOfPressure, float span, float chord, Airfoil airfoil) {
		this(centerOfPressure, span, chord, airfoil, new Vector3f(0, 1, 0), new Vector3f(1, 0, 0), 0.25f);
	}

	public Wing(Vector3fc centerOfPressure, float span, float chord, Airfoil airfoil, Vector3fc normal, Vector3fc spanDir) {
		this(centerOfPressure, span, chord, airfoil, normal, spanDir, 0.25f);
	}

	public Wing(Vector3fc centerOfPressure, float span, float chord, Airfoil airfoil, float flapRatio) {
		this(centerOfPressure, span, chord, airfoil, new Vector3f(0, 1, 0), new Vector3f(1, 0, 0), flapRatio);
	}

	public Wing(Vector3fc centerOfPressure, float span, float chord, Airfoil airfoil, Vector3fc normal, Vector3fc spanDir, float flapRatio) {
		this.centerOfPressure = centerOfPressure;
		this.span = span;
		this.chord = chord;

		this.area = span * chord;
		this.aspectRatio = span / chord;

		this.airfoil = airfoil;
		this.normal = normal;
		this.spanDir = spanDir;
		this.flapRatio = flapRatio;

		this.flapRatioSqrt = MathHelper.sqrt(flapRatio);

		// wings should not provide lift when flying local-sideways, rudder should not have effect when flying local-vertically
		this.activeWindComponents = new Vector3f(
			Math.min(Math.abs(normal.x()), 1),
			Math.min(Math.abs(normal.y()), 1),
			1
		);
	}

	public void setControlInput(float controlInput) {
		this.controlInput = Math.clamp(controlInput, -1.0f, 1.0f);
	}

	/* this is probably overcomplicated
	private static float airDensity(float altitude) {
		if (altitude <= 0) {
			return 1.225f;
		} else {
			float p = 100 * (1013.25f - altitude * (12/100f)); // absolute pressure (Pa). 1013.25 hPa at sea level, -12 hPa per 100m
			float R_specific = 287.0500676f; // J/(kg*K)
			float T = 288.15f; // absolute temperature (K) - 15 C at sea level
			return p / (R_specific * T);
		}
	}*/

	// https://www.jakobmaier.at/posts/flight-simulation/

	/**
	 * Apply lift and drag forces to the given rigid body.
	 * @param body the rigid body to apply forces to
	 * @param dt time step
	 * @param airDensity density in kg/m^3 of the air at the current altitude
	 * @param thermalSpeed airspeed of rising thermals in m/s
	 * @return whether forces were applied
	 */
	public boolean applyForcesTo(RigidBody body, float dt, float airDensity, float thermalSpeed) {
		Vector3f local_vel = body.velocityAtPoint(centerOfPressure);
		local_vel.sub(body.directionToLocal(new Vector3f(0, thermalSpeed, 0))); // add upwards thermals

		//float speed = local_vel.length(); // this provides lift when wing is perpendicular to the flow, which is bad
		// https://wiki.flightgear.org/JSBSim_Aerodynamics
		// we're calculating speedUW or speedUV, depending on if we are a wing or a rudder
		// TODO: perhaps split out wing control surfaces to be able to use speedU
		float speed = local_vel.mul(activeWindComponents, new Vector3f()).length();

		if (speed < 1e-6f) {
			return false; // no lift or drag at 'zero' speed
		}

		Vector3f drag_dir = new Vector3f(local_vel).mul(-1).normalize();
		/*if (new Vector3f(drag_dir).absolute().distanceSquared(absoluteNormal) < 1e-6f) {
			drag_dir.add(0, 0, 1e-6f);
			drag_dir.normalize();
		}*/

		if (!drag_dir.isFinite()) {
			return false;
		}

		Vector3f lift_dir = new Vector3f(drag_dir).cross(normal).cross(drag_dir).normalize();
		//Vector3f lift_dir = new Vector3f(spanDir).cross(drag_dir).normalize(); // perhaps this should be the other way around, and then it would work?
		if (!lift_dir.isFinite()) {
			lift_dir.set(0.0f);
		}

		float alpha = (float) Math.toDegrees(Math.asin(drag_dir.dot(normal)));
		float cl = airfoil.getCl(alpha);
		float cd = airfoil.getCd(alpha);

		if (flapRatio > 0.0f) {
			float f = local_vel.z >= 0 ? 1.0f : -1.0f;

			float delta_cl = flapRatioSqrt * airfoil.clMax * controlInput * f;
			cl += delta_cl;
		}

		float inducedDrag = (cl*cl) / (MathHelper.PI * aspectRatio * efficiencyFactor);
		cd += inducedDrag;

		float dynamic_pressure = 0.5f * speed * speed * airDensity * area;

		// WARN: lift_dir and drag_dir are no longer usable after these calls
		Vector3f lift = lift_dir.mul(cl * dynamic_pressure);
		Vector3f drag = drag_dir.mul(cd * dynamic_pressure);

		Vector3f forces = lift.add(drag);
		body.applyForceAtPoint(forces, centerOfPressure);

		return true;
	}

	/**
	 * Add wing to center of pressure calculation.
	 * @param centerOfPressure calculation vector to add to (local space position * ||force||)
	 * @param alpha angle of attack in degrees
	 * @return contributed force
	 */
	float contributeToCenterOfPressure(Vector3f centerOfPressure, float alpha) {
		float cl = airfoil.getCl(alpha);
		float lift = cl * area; // ignore air density and speed, since those are divided out in the end

		centerOfPressure.add(this.centerOfPressure.mul(lift, new Vector3f()));
		return lift;
	}
}
