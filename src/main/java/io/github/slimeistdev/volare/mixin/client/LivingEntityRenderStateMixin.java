package io.github.slimeistdev.volare.mixin.client;

import io.github.slimeistdev.volare.mixin_interfaces.LivingEntityRenderStateDuck;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements LivingEntityRenderStateDuck {
	@Unique
	private float volare$vehicleRoll;

	@Unique
	private float volare$vehiclePitch;

	@Override
	public float volare$getVehicleRoll() {
		return volare$vehicleRoll;
	}

	@Override
	public void volare$setVehicleRoll(float roll) {
		this.volare$vehicleRoll = roll;
	}

	@Override
	public float volare$getVehiclePitch() {
		return volare$vehiclePitch;
	}

	@Override
	public void volare$setVehiclePitch(float pitch) {
		this.volare$vehiclePitch = pitch;
	}
}
