package io.github.slimeistdev.volare.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.slimeistdev.volare.config.VolareClientConfig;
import io.github.slimeistdev.volare.infrastructure.QuatEntity;
import io.github.slimeistdev.volare.util.MathUtil;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.util.math.MathHelper.RADIANS_PER_DEGREE;

@Mixin(Camera.class)
public class CameraMixin {
	@Shadow
	private float lastCameraY;
	@Shadow
	private float cameraY;

	@Unique
	private MathUtil.EulerAngles volare$eulerAngles;

	@Inject(method = "update", at = @At("HEAD"))
	private void clearRoll(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
		volare$eulerAngles = null;
	}

	@Inject(
		method = "update",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V",
			ordinal = 1
		)
	)
	private void updateRoll(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
		Entity rootVehicle = focusedEntity.getRootVehicle();
		if (rootVehicle != focusedEntity && rootVehicle instanceof QuatEntity quatEntity) {
			var config = VolareClientConfig.get();
			if (thirdPerson ? config.rollCamera3rdPerson : config.rollCamera1stPerson) {
				volare$eulerAngles = new MathUtil.EulerAngles(
					-rootVehicle.getYaw(tickProgress),
					-rootVehicle.getPitch(tickProgress),
					quatEntity.getRoll(tickProgress)
				);
			}
		}
	}

	@WrapOperation(
		method = "update",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V",
				ordinal = 1
			)
		)
	)
	private void applyRollToCameraOffset(Camera instance, double x, double y, double z, Operation<Void> original, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress) {
		if (volare$eulerAngles != null) {
			float eyeHeight = MathHelper.lerp(tickProgress, lastCameraY, cameraY);

			Vector3f eyeOffset = new Vector3f(0, eyeHeight, 0);
			Quaternionf eyeRotation = volare$eulerAngles.getQuat();
			eyeOffset.rotate(eyeRotation);

			x += eyeOffset.x;
			y += eyeOffset.y - eyeHeight;
			z += eyeOffset.z;
		}

		original.call(instance, x, y, z);
	}

	@WrapOperation(method = "setRotation", at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;", remap = false))
	private Quaternionf applyRoll(Quaternionf instance, float angleY, float angleX, float angleZ, Operation<Quaternionf> original) {
		// application order:
		// vehicle yaw, pitch, roll
		// camera yaw, pitch, roll

		if (volare$eulerAngles == null)
			return original.call(instance, angleY, angleX, angleZ);

		float vehicleYaw = (180 + volare$eulerAngles.yaw()) * RADIANS_PER_DEGREE;
		float vehiclePitch = volare$eulerAngles.pitch() * RADIANS_PER_DEGREE;
		float vehicleRoll = volare$eulerAngles.roll() * RADIANS_PER_DEGREE;

		float deltaYaw = angleY - vehicleYaw;

		original.call(instance, vehicleYaw, vehiclePitch, vehicleRoll);
		Quaternionf cameraRot = new Quaternionf().rotateYXZ(deltaYaw, angleX, angleZ);

		return instance.mul(cameraRot);
	}
}
