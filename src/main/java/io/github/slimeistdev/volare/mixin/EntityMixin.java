package io.github.slimeistdev.volare.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.slimeistdev.volare.content.glider.GliderEntity;
import io.github.slimeistdev.volare.infrastructure.QuatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public abstract Entity getRootVehicle();

	@Shadow
	public abstract double getX();

	@Shadow
	public abstract double getY();

	@Shadow
	public abstract double getZ();

	@Shadow
	public abstract World getWorld();

	@Shadow
	public abstract @Nullable Entity getVehicle();

	@WrapOperation(
		method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity$PositionUpdater;accept(Lnet/minecraft/entity/Entity;DDD)V"
		)
	)
	private void rollPassengers(Entity.PositionUpdater instance, Entity passenger, double x, double y, double z, Operation<Void> original) {
		if (this.getRootVehicle() instanceof QuatEntity quatEntity) {
			Quaternionfc quat = getWorld().isClient ? quatEntity.getQuatClient() : quatEntity.getQuat();
			Vector3d offset = quat.transform(
				x - getX(),
				y - getY(),
				z - getZ(),
				new Vector3d()
			);

			original.call(instance, passenger, offset.x + getX(), offset.y + getY(), offset.z + getZ());
		} else {
			original.call(instance, passenger, x, y, z);
		}
	}

	@WrapMethod(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z")
	private boolean nestedPassengerApplyGliderValues(Entity entity, boolean force, Operation<Boolean> original) {
		boolean ret = original.call(entity, force);

		if (ret && this.getRootVehicle() instanceof GliderEntity glider && glider != getVehicle()) {
			glider.handlePassengerAddition((Entity) (Object) this);
		}

		return ret;
	}

	@WrapMethod(method = "dismountVehicle")
	private void nestedPassengerRemoveGliderValues(Operation<Void> original) {
		GliderEntity originalGlider = this.getRootVehicle() instanceof GliderEntity glider ? glider : null;

		original.call();

		if (originalGlider != null && !(this.getRootVehicle() instanceof GliderEntity)) {
			originalGlider.handlePassengerRemoval((Entity) (Object) this);
		}
	}
}
