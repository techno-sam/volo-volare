/*
package io.github.slimeistdev.volare.mixin.roll;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.slimeistdev.volare.infrastructure.QuatEntity;
import io.github.slimeistdev.volare.infrastructure.network.S2CPacket;
import io.github.slimeistdev.volare.network.VolarePackets;
import io.github.slimeistdev.volare.network.s2c.EntityPositionSyncS2CPacket_Roll;
import io.github.slimeistdev.volare.network.s2c.EntityS2CPacket_Rotate_Roll;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public class MixinEntityTrackerEntry {
	@Shadow
	@Final
	private Entity entity;
	@Shadow
	@Final
	private Consumer<Packet<?>> watchingSender;
	@Unique
	private byte volare$lastRoll;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initRoll(ServerWorld world, Entity entity, int tickInterval, boolean alwaysUpdateVelocity, Consumer<Packet<?>> watchingSender, BiConsumer<Packet<?>, List<UUID>> filteredWatchingSender, CallbackInfo ci) {
		if (entity instanceof QuatEntity quatEntity) {
			volare$lastRoll = MathHelper.packDegrees(quatEntity.getRoll());
		}
	}

	// boolean bl = @(Math.abs(b - this.lastYaw) >= 1) || Math.abs(c - this.lastPitch) >= 1;
	@Definition(id = "abs", method = "Ljava/lang/Math;abs(I)I")
	@Definition(id = "lastYaw", field = "Lnet/minecraft/server/network/EntityTrackerEntry;lastYaw:B")
	@Expression("abs(? - this.lastYaw) >= 1")
	@ModifyExpressionValue(
		method = "tick",
		at = @At("MIXINEXTRAS:EXPRESSION"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;packDegrees(F)B", ordinal = 0),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasVehicle()Z")
		)
	)
	private boolean includeRollChanges(boolean original) {
		if (!original && entity instanceof QuatEntity quatEntity) {
			byte r = MathHelper.packDegrees(quatEntity.getRoll());
			return Math.abs(r - volare$lastRoll) >= 1;
		}

		return original;
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
	private <T> void sendRollChanges(Consumer<T> instance, T object, Operation<Void> original) {
		if (instance == watchingSender && entity instanceof QuatEntity quatEntity) {
			float roll = quatEntity.getRoll();
			byte r = MathHelper.packDegrees(roll);

			S2CPacket packet;
			if (object instanceof EntityPositionSyncS2CPacket) {
				packet = new EntityPositionSyncS2CPacket_Roll(entity.getId(), roll);
				volare$lastRoll = r;
			} else if (object instanceof EntityS2CPacket.Rotate || object instanceof EntityS2CPacket.RotateAndMoveRelative) {
				packet = new EntityS2CPacket_Rotate_Roll(entity.getId(), r);
				volare$lastRoll = r;
			} else {
				packet = null;
			}

			if (packet != null) {
				watchingSender.accept(VolarePackets.PACKETS.tunnelPacket(packet));
			}
		}

		original.call(instance, object);
	}
}
*/
