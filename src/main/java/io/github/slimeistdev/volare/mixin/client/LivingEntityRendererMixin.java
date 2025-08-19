package io.github.slimeistdev.volare.mixin.client;

import io.github.slimeistdev.volare.infrastructure.QuatEntity;
import io.github.slimeistdev.volare.mixin_interfaces.LivingEntityRenderStateDuck;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
	@Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
	private void updateVehicleRollState(T entity, S state, float tickProgress, CallbackInfo ci) {
		float roll;
		float pitch;
		float yaw;
		Entity rootVehicle = entity.getRootVehicle();
		if (rootVehicle != entity && rootVehicle instanceof QuatEntity quatEntity) {
			roll = quatEntity.getRoll(tickProgress);
			pitch = rootVehicle.getPitch(tickProgress);
			yaw = rootVehicle.getYaw(tickProgress);
		} else {
			roll = 0.0f;
			pitch = 0.0f;
			yaw = 0.0f;
		}
		((LivingEntityRenderStateDuck) state).volare$setVehicleRoll(roll);
		((LivingEntityRenderStateDuck) state).volare$setVehiclePitch(pitch);
		((LivingEntityRenderStateDuck) state).volare$setVehicleYaw(yaw);
	}

	@Inject(method = "setupTransforms", at = @At("RETURN"))
	private void setupRollTransforms(S state, MatrixStack matrices, float bodyYaw, float baseHeight, CallbackInfo ci) {
		float roll = ((LivingEntityRenderStateDuck) state).volare$getVehicleRoll();
		float pitch = ((LivingEntityRenderStateDuck) state).volare$getVehiclePitch();
		if (roll == 0.0f && pitch == 0.0f) return;

		float vehicleYaw = ((LivingEntityRenderStateDuck) state).volare$getVehicleYaw();
		float deltaYaw = vehicleYaw - bodyYaw;

		// unrotate
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-deltaYaw));

		matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(pitch));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));

		// re-rotate
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(deltaYaw));
	}
}
