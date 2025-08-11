package io.github.slimeistdev.volare.content.glider.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class GliderEntityModel extends EntityModel<GliderRenderState> {
	private static final boolean DEBUG = Boolean.getBoolean("volare.debug.glider");

	private final @Nullable ModelPart com;
	private final @Nullable ModelPart cop;

	private final ModelPart aileronLeft;
	private final ModelPart aileronRight;

	private final ModelPart elevatorLeft;
	private final ModelPart elevatorRight;

	private final ModelPart rudder;

	protected GliderEntityModel(ModelPart root) {
		super(root);

		ModelPart com, cop;
		if (root.hasChild("CoM") && root.hasChild("CoP")) {
			com = root.getChild("CoM");
			cop = root.getChild("CoP");

			com.hidden = !DEBUG;
			cop.hidden = !DEBUG;
		} else {
			com = null;
			cop = null;
		}

		this.com = DEBUG ? com : null;
		this.cop = DEBUG ? cop : null;

		var fuselage = root.getChild("fuselage");

		aileronLeft = fuselage.getChild("wing_left").getChild("aileron_left");
		aileronRight = fuselage.getChild("wing_right").getChild("aileron_right");

		elevatorLeft = fuselage.getChild("horizontal_stabilizer").getChild("elevator_left");
		elevatorRight = fuselage.getChild("horizontal_stabilizer").getChild("elevator_right");

		rudder = fuselage.getChild("vertical_stabilizer").getChild("rudder");
	}

	@Override
	public void setAngles(GliderRenderState state) {
		super.setAngles(state);

		if (com != null) {
			var centerOfMass = new Vector3f(state.centerOfMass).mul(16.0f).sub(0.5f, 0.5f, 0.5f);
			com.setOrigin(-centerOfMass.x, -centerOfMass.y, centerOfMass.z);
		}

		if (cop != null) {
			var centerOfPressure = new Vector3f(state.centerOfPressure).mul(16.0f).sub(0.5f, 0.5f, 0.5f);
			cop.setOrigin(-centerOfPressure.x, -centerOfPressure.y, centerOfPressure.z);
		}

		aileronLeft.setAngles(state.aileronAngle, 0, 0);
		aileronRight.setAngles(-state.aileronAngle, 0, 0);

		elevatorLeft.setAngles(-state.elevatorAngle, 0, 0);
		elevatorRight.setAngles(-state.elevatorAngle, 0, 0);

		rudder.setAngles(0, state.rudderAngle, 0);
	}
}
