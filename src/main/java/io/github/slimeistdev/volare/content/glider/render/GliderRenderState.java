package io.github.slimeistdev.volare.content.glider.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public class GliderRenderState extends EntityRenderState {
	public Quaternionfc quat;

	public Vector3fc centerOfMass;
	public Vector3fc centerOfPressure;

	public float propellerAngle;

	public int damageWobbleSide;
	public float damageWobbleTicks;
	public float damageWobbleStrength;
}
