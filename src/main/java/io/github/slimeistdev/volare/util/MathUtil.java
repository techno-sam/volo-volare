package io.github.slimeistdev.volare.util;

import org.joml.*;

import static net.minecraft.util.math.MathHelper.DEGREES_PER_RADIAN;
import static net.minecraft.util.math.MathHelper.RADIANS_PER_DEGREE;

public class MathUtil {
	public static EulerAngles toEuler(Quaternionfc quat) {
		Vector3f vec3 = quat.getEulerAnglesYXZ(new Vector3f());
		return new EulerAngles(180 + (vec3.y * DEGREES_PER_RADIAN), vec3.x * DEGREES_PER_RADIAN, vec3.z * DEGREES_PER_RADIAN);
	}

	/**
	 * @param yaw degrees
	 * @param pitch degrees
	 * @param roll degrees
	 */
	public record EulerAngles(float yaw, float pitch, float roll) {
		public Quaternionf getQuat() {
			Quaternionf quat = new Quaternionf();
			quat.rotateYXZ((180 + yaw) * RADIANS_PER_DEGREE, pitch * RADIANS_PER_DEGREE, roll * RADIANS_PER_DEGREE);
			return quat;
		}
	}

	public static Matrix3f rowMajorMatrix(
		float m00, float m10, float m20,
		float m01, float m11, float m21,
		float m02, float m12, float m22
	) {
		return new Matrix3f(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}

	public static Matrix3f outerProduct(Vector3fc u, Vector3fc v) {
		return rowMajorMatrix(
			u.x() * v.x(), u.x() * v.y(), u.x() * v.z(),
			u.y() * v.x(), u.y() * v.y(), u.y() * v.z(),
			u.z() * v.x(), u.z() * v.y(), u.z() * v.z()
		);
	}

	public static Matrix3f translateInertiaTensor(Matrix3fc comInertia, float mass, Vector3fc translation) {
		return translateInertiaTensor(comInertia, mass, translation, new Matrix3f());
	}

	public static Matrix3f translateInertiaTensor(Matrix3fc comInertia, float mass, Vector3fc translation, Matrix3f dest) {
		// https://en.wikipedia.org/wiki/Moment_of_inertia#Inertia_tensor_of_translation
		final Matrix3f identity = new Matrix3f();
		final float dot = translation.dot(translation);
		final Matrix3f outerTranslation = outerProduct(translation, translation);

		return comInertia.add(identity.scale(dot).sub(outerTranslation), dest);
	}
}
