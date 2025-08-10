package io.github.slimeistdev.volare.network.c2s;

import io.github.slimeistdev.volare.content.glider.GliderEntity;
import io.github.slimeistdev.volare.content.glider.PhysicsSnapshot;
import io.github.slimeistdev.volare.infrastructure.network.C2SPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayerEntity;

public record SetGliderPhysicsC2SPacket(PhysicsSnapshot snapshot) implements C2SPacket {
	public static final PacketCodec<ByteBuf, SetGliderPhysicsC2SPacket> PACKET_CODEC = PhysicsSnapshot.PACKET_CODEC.xmap(
		SetGliderPhysicsC2SPacket::new,
		SetGliderPhysicsC2SPacket::snapshot
	);

	public SetGliderPhysicsC2SPacket(GliderEntity glider) {
		this(glider.createPhysicsSnapshot());
	}

	@Override
	public void handle(ServerPlayerEntity sender) {
		if (sender.getControllingVehicle() instanceof GliderEntity glider) {
			glider.applyPhysicsSnapshot(snapshot);
		}
	}
}
