package io.github.slimeistdev.volare.network;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.infrastructure.network.PacketSet;
import io.github.slimeistdev.volare.network.c2s.RotationC2SPacket;
import io.github.slimeistdev.volare.network.c2s.SetGliderPhysicsC2SPacket;
import io.github.slimeistdev.volare.network.s2c.SetGliderPhysicsS2CPacket;

public class VolarePackets {
	public static final PacketSet PACKETS = PacketSet.builder(Volare.ID, 1)
		.c2s(RotationC2SPacket.class, RotationC2SPacket.PACKET_CODEC)
		.c2s(SetGliderPhysicsC2SPacket.class, SetGliderPhysicsC2SPacket.PACKET_CODEC)

		.s2c(SetGliderPhysicsS2CPacket.class, SetGliderPhysicsS2CPacket.PACKET_CODEC)

		.build();
}
