package io.github.slimeistdev.volare.network;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.infrastructure.network.PacketSet;
import io.github.slimeistdev.volare.network.c2s.UpdateGliderC2SPacket;
import io.github.slimeistdev.volare.network.s2c.SetGliderPhysicsS2CPacket;

public class VolarePackets {
	public static final PacketSet PACKETS = PacketSet.builder(Volare.ID, 1)
		.c2s(UpdateGliderC2SPacket.class, UpdateGliderC2SPacket.PACKET_CODEC)

		.s2c(SetGliderPhysicsS2CPacket.class, SetGliderPhysicsS2CPacket.PACKET_CODEC)

		.build();
}
