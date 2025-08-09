package io.github.slimeistdev.volare.content.glider.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class GliderEntityModel extends EntityModel<GliderRenderState> {
	private final ModelPart com;
	private final ModelPart cop;

	private final ModelPart fuselage;
	private final ModelPart propeller;

	protected GliderEntityModel(ModelPart root) {
		super(root);

		com = root.getChild("CoM");
		cop = root.getChild("CoP");

		fuselage = root.getChild("fuselage");
		propeller = fuselage.getChild("propeller");
	}

	@Override
	public void setAngles(GliderRenderState state) {
		super.setAngles(state);

		propeller.roll = state.propellerAngle * MathHelper.PI / 180.0f;

		var centerOfMass = new Vector3f(state.centerOfMass).mul(16.0f).sub(0.5f, 0.5f, 0.5f);
		com.setOrigin(-centerOfMass.x, -centerOfMass.y, centerOfMass.z);

		var centerOfPressure = new Vector3f(state.centerOfPressure).mul(16.0f).sub(0.5f, 0.5f, 0.5f);
		cop.setOrigin(-centerOfPressure.x, -centerOfPressure.y, centerOfPressure.z);
	}
}
