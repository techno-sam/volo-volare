package io.github.slimeistdev.volare;

import io.github.slimeistdev.volare.content.glider.GliderParams;
import org.joml.Vector3fc;

/**
 * A simple entrypoint to quickly check glider-related code.
 */
public class GliderCheck {
	private static String pixelCoords(Vector3fc vec) {
		return String.format("(%f, %f, %f)", vec.x() * 16, vec.y() * 16, vec.z() * 16);
	}

	public static void main(String[] args) {
		GliderParams params = GliderParams.create();

		System.out.println("Glider mass: " + params.body().mass + " kg");
		System.out.println("Glider center of mass: " + pixelCoords(params.centerOfMass()));
		System.out.println("Glider center of pressure: " + pixelCoords(params.centerOfPressure()));
	}
}
