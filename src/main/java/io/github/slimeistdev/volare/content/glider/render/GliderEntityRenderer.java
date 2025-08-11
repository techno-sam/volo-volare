package io.github.slimeistdev.volare.content.glider.render;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.GliderEntity;
import io.github.slimeistdev.volare.registry.client.VolareEntityRenderers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import static net.minecraft.util.math.MathHelper.RADIANS_PER_DEGREE;

@Environment(EnvType.CLIENT)
public class GliderEntityRenderer extends EntityRenderer<GliderEntity, GliderRenderState> {
	private static final Identifier TEXTURE = Volare.id("textures/entity/glider/glider.png");

	protected final GliderEntityModel model;

	public GliderEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.model = new GliderEntityModel(context.getPart(VolareEntityRenderers.GLIDER_LAYER));
	}

	@Override
	public void render(GliderRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();

		matrices.scale(-1.0F, -1.0F, 1.0F);

		/*matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - state.yaw));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(state.pitch));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(state.roll));*/

		//matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
		matrices.translate(0, -state.centerOfMass.y(), 0);

		matrices.multiply(state.quat);

		float wobbleTicks = state.damageWobbleTicks;
		if (wobbleTicks > 0.0F) {
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(
				MathHelper.sin(wobbleTicks)
					* wobbleTicks
					* state.damageWobbleStrength / 10.0F
					* state.damageWobbleSide
			));
		}

		matrices.translate(state.centerOfMass.x(), state.centerOfMass.y(), -state.centerOfMass.z());

		this.model.setAngles(state);
		this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(TEXTURE)), light, OverlayTexture.DEFAULT_UV);

		matrices.pop();

		super.render(state, matrices, vertexConsumers, light);
	}

	@Override
	public GliderRenderState createRenderState() {
		return new GliderRenderState();
	}

	@Override
	public void updateRenderState(GliderEntity entity, GliderRenderState state, float tickProgress) {
		super.updateRenderState(entity, state, tickProgress);

		state.quat = entity.getQuatClient(tickProgress);

		state.centerOfMass = entity.getCenterOfMass();
		state.centerOfPressure = entity.getCenterOfPressure();

		state.aileronAngle = entity.getAileronAngle(tickProgress) * RADIANS_PER_DEGREE;
		state.elevatorAngle = entity.getElevatorAngle(tickProgress) * RADIANS_PER_DEGREE;
		state.rudderAngle = entity.getRudderAngle(tickProgress) * RADIANS_PER_DEGREE;

		state.damageWobbleTicks = entity.getDamageWobbleTicks() - tickProgress;
		state.damageWobbleSide = entity.getDamageWobbleSide();
		state.damageWobbleStrength = Math.max(0.0f, entity.getDamageWobbleStrength() - tickProgress);
	}
}
