package io.github.slimeistdev.volare.content.glider;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @param body a pre-configured {@link RigidBody} representing the glider's fuselage and wings
 * @param wings a {@link GliderWings} instance containing the glider's wings and control surfaces
 * @param centerOfMass the center of mass of the glider in model space (meter-scale, not pixel-scale)
 * @param centerOfPressure the center of pressure of the glider in model space (meter-scale, not pixel-scale)
 */
public record GliderParams(RigidBody body, GliderWings wings, Vector3fc centerOfMass, Vector3fc centerOfPressure) {
	private static final float BALSA_DENSITY = 2.0f; // kg/m^3 (160 originally, this is hacked to be lighter because why not)

	// glider design based on https://en.wikipedia.org/wiki/Rolladen-Schneider_LS4
	// https://www.waikerieglidingclub.com.au/ls-4/
	// https://vezc.aero/vloot/VR
	public static GliderParams create() {
		RigidBodyParams params = RigidBodyParams.builder()
			.addVoxelBox( // fuselage
				-1, 0, -8,
				2, 2, 29,
				BALSA_DENSITY
			).tagLast("fuselage")
			.addVoxelBox( // left wing
				-32, 1.5f, -2,
				32, 1, 3,
				BALSA_DENSITY
			).tagLast("left_wing")
			.addVoxelBox( // right wing
				0, 1.5f, -2,
				32, 1, 3,
				BALSA_DENSITY
			).tagLast("right_wing")
			.addVoxelBox( // tail
				-0.5f, 1.8f, 17.987f,
				1, 5, 3,
				BALSA_DENSITY
			).tagLast("tail")
			.addVoxelBox( // left elevator
				-8, 6.5f, 18.65f,
				8, 1, 3,
				BALSA_DENSITY
			).tagLast("left_elevator")
			.addVoxelBox( // right elevator
				0, 6.5f, 18.65f,
				8, 1, 3,
				BALSA_DENSITY
			).tagLast("right_elevator")
			.addPointMass(
				new Vector3f(0.0f, 1.0f, -7f).mul(1 / 16f),
				0.05f // 0.05f for CoP aft of CoM
			).tagLast("balance")
			.build();

		RigidBody rigidBody = new RigidBody(params);

		Vector3fc scaling = new Vector3f(1, 1, -1); // we flip z velocity elsewhere to make things right-handed, so we have to do that too here

		// conventional wisdom says center of pressure is at 1/4 chord length from the leading edge
		// the offset value is the center, so we need to move 1/4 chord length forward (-z)
		Vector3fc leftAileronPos = new Vector3f(params.offset("left_wing")).sub(0, 0, (3 / 16f) * 0.25f).mul(scaling);
		Vector3fc rightAileronPos = new Vector3f(params.offset("right_wing")).sub(0, 0, (3 / 16f) * 0.25f).mul(scaling);
		Vector3fc elevatorPos = new Vector3f(params.offset("left_elevator"))
			.add(params.offset("right_elevator"))
			.mul(0.5f)
			.sub(0, 0, (3 / 16f) * 0.25f)
			.mul(scaling);
		Vector3fc tailPos = new Vector3f(params.offset("tail")).mul(scaling);

		GliderWings wings = new GliderWings(
			new Wing(leftAileronPos, 32 / 16f, 3 / 16f, Airfoils.NACA2412),
			new Wing(rightAileronPos, 32 / 16f, 3 / 16f, Airfoils.NACA2412),
			new Wing(elevatorPos, 16 / 16f, 3 / 16f, Airfoils.NACA2412, 0.5f),
			new Wing(tailPos, 5 / 16f, 3 / 16f, Airfoils.N0012, new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), 0.5f)
		);

		Vector3fc centerOfMass = params.centerOfMass();
		Vector3f centerOfPressure = new Vector3f();
		wings.calculateCenterOfPressure(5.0f, centerOfPressure);
		centerOfPressure.mul(scaling);
		centerOfPressure.add(centerOfMass);

		return new GliderParams(rigidBody, wings, centerOfMass, centerOfPressure);
	}
}
