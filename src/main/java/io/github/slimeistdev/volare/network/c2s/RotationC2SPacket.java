package io.github.slimeistdev.volare.network.c2s;

import io.github.slimeistdev.volare.content.glider.GliderEntity;
import io.github.slimeistdev.volare.infrastructure.network.C2SPacket;
import io.github.slimeistdev.volare.util.MathUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public record RotationC2SPacket(Quaternionf quat) implements C2SPacket {
	public static final PacketCodec<ByteBuf, RotationC2SPacket> PACKET_CODEC = PacketCodecs.QUATERNION_F.xmap(RotationC2SPacket::new, RotationC2SPacket::quat);

	public RotationC2SPacket(Quaternionfc quat) {
		this(new Quaternionf(quat));
	}

	@Override
	public void handle(ServerPlayerEntity sender) {
		if (sender.getControllingVehicle() instanceof GliderEntity glider) {
			glider.setQuat(quat);

			var euler = MathUtil.toEuler(glider.getQuat());
			glider.setPitch(euler.pitch());
			glider.setYaw(euler.yaw());

			glider.setQuatClient(quat);
		}
	}
}
