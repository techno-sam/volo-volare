package io.github.slimeistdev.volare.network.s2c;

import io.github.slimeistdev.volare.content.glider.GliderEntity;
import io.github.slimeistdev.volare.content.glider.PhysicsSnapshot;
import io.github.slimeistdev.volare.infrastructure.network.S2CPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record SetGliderPhysicsS2CPacket(int id, PhysicsSnapshot snapshot) implements S2CPacket {
	public static final PacketCodec<ByteBuf, SetGliderPhysicsS2CPacket> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.VAR_INT,
		SetGliderPhysicsS2CPacket::id,
		PhysicsSnapshot.PACKET_CODEC,
		SetGliderPhysicsS2CPacket::snapshot,
		SetGliderPhysicsS2CPacket::new
	);

	public SetGliderPhysicsS2CPacket(GliderEntity glider) {
		this(glider.getId(), glider.createPhysicsSnapshot());
	}

	@Override
	public void handle(MinecraftClient mc) {
		if (mc.world != null && mc.world.getEntityById(id) instanceof GliderEntity glider) {
			glider.applyPhysicsSnapshot(snapshot);
		}
	}
}
