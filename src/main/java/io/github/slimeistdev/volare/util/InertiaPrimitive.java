package io.github.slimeistdev.volare.util;

import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3f;
import org.joml.Vector3fc;

public interface InertiaPrimitive {
	/** Calculate the inertia tensor around the Center of Mass */
	@ApiStatus.NonExtendable
	default Matrix3f calculateCoMTensor() {
		return calculateCoMTensor(new Matrix3f());
	}

	/** Calculate the inertia tensor around the Center of Mass */
	Matrix3f calculateCoMTensor(Matrix3f dest);

	float mass();

	/**
	 * A point mass
	 * This is, on its own, an inherently-useless tensor function (since a point mass has no rotational inertia around its CoM)
	 * @param mass kg
	 */
	record PointMass(float mass) implements InertiaPrimitive {
		@Override
		public Matrix3f calculateCoMTensor(Matrix3f dest) {
			return dest.set(
				0, 0, 0,
				0, 0, 0,
				0, 0, 0
			);
		}
	}

	/**
	 * A solid rectangular prism
	 * @param density kg/m^3
	 * @param radii axial radii, (m, m, m)
	 */
	record RectangularPrism(float density, Vector3fc radii) implements InertiaPrimitive {
		@Override
		public Matrix3f calculateCoMTensor(Matrix3f dest) {
			float densityFactor = 8.f/3.f * density;
			float x = radii.x();
			float y = radii.y();
			float z = radii.z();

			float x3 = x*x*x;
			float y3 = y*y*y;
			float z3 = z*z*z;

			float xx = densityFactor * (x*y3*z + x*y*z3);
			float yy = densityFactor * (x3*y*z + x*y*z3);
			float zz = densityFactor * (x3*y*z + x*y3*z);

			return dest.set(
				xx, 0, 0,
				0, yy, 0,
				0, 0, zz
			);
		}

		@Override
		public float mass() {
			return 8 * radii.x() * radii.y() * radii.z() * density;
		}
	}
}
