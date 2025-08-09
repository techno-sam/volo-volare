package io.github.slimeistdev.volare.network;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.infrastructure.network.PacketSet;
import io.github.slimeistdev.volare.network.c2s.RotationC2SPacket;

public class VolarePackets {
	public static final PacketSet PACKETS = PacketSet.builder(Volare.ID, 1)
		.c2s(RotationC2SPacket.class, RotationC2SPacket.PACKET_CODEC)

		.build();
}
