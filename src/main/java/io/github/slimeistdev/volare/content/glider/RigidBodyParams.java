package io.github.slimeistdev.volare.content.glider;

import io.github.slimeistdev.volare.util.InertiaPrimitive;
import io.github.slimeistdev.volare.util.MathUtil;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RigidBodyParams(float mass, Vector3fc centerOfMass, Matrix3fc inertia, Map<String, Vector3fc> partOffsets) {
	private static final Vector3fc DEFAULT_OFFSET = new Vector3f(0);

	public Vector3fc offset(String partName) {
		return partOffsets.getOrDefault(partName, DEFAULT_OFFSET);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private record InertialObject(InertiaPrimitive primitive, Vector3fc location) {}

		private float mass = 0;
		private final Vector3f scaledCoM = new Vector3f();
		private final List<InertialObject> inertialObjects = new ArrayList<>();
		private final Map<String, Vector3fc> rawPartOffsets = new HashMap<>();

		private Builder() {}

		/** Add a string tag to the last added part. Throws if none yet added */
		public Builder tagLast(String name) {
			rawPartOffsets.put(name, inertialObjects.getLast().location());
			return this;
		}

		public Builder addPrimitive(Vector3fc location, InertiaPrimitive primitive) {
			float mass = primitive.mass();
			assert mass > 0;
			this.mass += mass;
			this.scaledCoM.add(location.mul(mass, new Vector3f()));
			this.inertialObjects.add(new InertialObject(primitive, location));
			return this;
		}

		public Builder addPointMass(Vector3fc location, float mass) {
			return addPrimitive(location, new InertiaPrimitive.PointMass(mass));
		}

		public Builder addBox(Vector3fc location, float density, Vector3fc radii) {
			return addPrimitive(location, new InertiaPrimitive.RectangularPrism(density, radii));
		}

		/**
		 * Add 1/16 scale box
		 * @param cornerX corner x, in pixel coords
		 * @param cornerY corner y, in pixel coords
		 * @param cornerZ corner z, in pixel coords
		 * @param sizeX width, in pixel coords
		 * @param sizeY height, in pixel coords
		 * @param sizeZ length, in pixel coords
		 * @param density kg/m^3
		 * @return this
		 */
		public Builder addVoxelBox(
			float cornerX, float cornerY, float cornerZ,
			float sizeX, float sizeY, float sizeZ,
			float density
		) {
			Vector3f radii = new Vector3f(sizeX, sizeY, sizeZ).mul(0.5f);
			Vector3f center = new Vector3f(cornerX, cornerY, cornerZ).add(radii);
			return addBox(center.mul(1 / 16f), density, radii.mul(1 / 16f));
		}

		public RigidBodyParams build() {
			assert mass > 0 : "Cannot build empty rigid body";

			Vector3fc com = scaledCoM.div(mass, new Vector3f());
			Matrix3f inertia = new Matrix3f();

			Matrix3f scratchMat = new Matrix3f();
			Vector3f scratchVec = new Vector3f();
			for (var obj : inertialObjects) {
				Vector3f location = obj.location().sub(com, scratchVec);
				Matrix3f comInertia = obj.primitive().calculateCoMTensor(scratchMat);
				Matrix3f objInertia = MathUtil.translateInertiaTensor(comInertia, obj.primitive().mass(), location, scratchMat);

				inertia.add(objInertia);
			}

			HashMap<String, Vector3fc> partOffsets = new HashMap<>();
			for (var entry : rawPartOffsets.entrySet()) {
				partOffsets.put(entry.getKey(), entry.getValue().sub(com, new Vector3f()));
			}

			inertia.mulComponentWise(new Matrix3f()); // temporarily remove coupling for simplicity

			return new RigidBodyParams(mass, com, inertia, partOffsets);
		}
	}
}
