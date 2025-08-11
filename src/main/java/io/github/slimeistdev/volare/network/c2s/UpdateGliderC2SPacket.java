package io.github.slimeistdev.volare.network.c2s;

import io.github.slimeistdev.volare.content.glider.GliderEntity;
import io.github.slimeistdev.volare.content.glider.PhysicsSnapshot;
import io.github.slimeistdev.volare.infrastructure.network.C2SPacket;
import io.github.slimeistdev.volare.util.MathUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import org.joml.Quaternionf;

public record UpdateGliderC2SPacket(Quaternionf orientation, PhysicsSnapshot snapshot, int pitchControl, int yawControl) implements C2SPacket {
	public static final PacketCodec<ByteBuf, UpdateGliderC2SPacket> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.QUATERNION_F,
		UpdateGliderC2SPacket::orientation,
		PhysicsSnapshot.PACKET_CODEC,
		UpdateGliderC2SPacket::snapshot,
		PacketCodecs.VAR_INT,
		UpdateGliderC2SPacket::pitchControl,
		PacketCodecs.VAR_INT,
		UpdateGliderC2SPacket::yawControl,
		UpdateGliderC2SPacket::new
	);

	@Override
	public void handle(ServerPlayerEntity sender) {
		if (sender.getControllingVehicle() instanceof GliderEntity glider) {
			glider.setQuat(orientation);

			var euler = MathUtil.toEuler(glider.getQuat());
			glider.setPitch(euler.pitch());
			glider.setYaw(euler.yaw());

			glider.setQuatClient(orientation);

			glider.applyPhysicsSnapshot(snapshot);
			glider.setControls(pitchControl, yawControl);
		}
	}
}
