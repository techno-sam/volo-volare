package io.github.slimeistdev.volare.content.glider.render;

import io.github.slimeistdev.volare.content.glider.components.GliderVariant;
import io.github.slimeistdev.volare.infrastructure.QuatEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.Identifier;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public class GliderRenderState extends EntityRenderState implements QuatEntity.RenderState {
	private static final Identifier DEFAULT_TEXTURE = GliderVariant.DEFAULT.assetInfo().texturePath();
	public Identifier texture = DEFAULT_TEXTURE;

	public Quaternionfc quat;

	public Vector3fc centerOfMass;
	public Vector3fc centerOfPressure;

	public float aileronAngle;
	public float elevatorAngle;
	public float rudderAngle;

	public int damageWobbleSide;
	public float damageWobbleTicks;
	public float damageWobbleStrength;

	@Override
	public Quaternionfc getQuat() {
		return this.quat;
	}

	@Override
	public void setQuat(Quaternionfc quat) {
		this.quat = quat;
	}
}
