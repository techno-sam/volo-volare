package io.github.slimeistdev.volare.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public class VolarePacketCodecs {
	public static final PacketCodec<PacketByteBuf, PacketByteBuf> NESTED_BUF_PACKET_CODEC = PacketCodec.ofStatic(
		(buf, value) -> buf.writeByteArray(value.array()),
		buf -> {
			var value = new PacketByteBuf(Unpooled.buffer());
			value.writeBytes(buf.readByteArray());
			return value;
		}
	);
}
