package io.github.slimeistdev.volare.network.s2c;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import io.github.slimeistdev.volare.config.VolareServerConfig;
import io.github.slimeistdev.volare.config.serializers.NetworkConfigSerializer;
import io.github.slimeistdev.volare.infrastructure.network.S2CPacket;
import io.github.slimeistdev.volare.util.VolarePacketCodecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record SetConfigS2CPacket(PacketByteBuf serializedConfig) implements S2CPacket {
	public static final PacketCodec<PacketByteBuf, SetConfigS2CPacket> PACKET_CODEC = VolarePacketCodecs.NESTED_BUF_PACKET_CODEC
		.xmap(
			SetConfigS2CPacket::new,
			SetConfigS2CPacket::serializedConfig
		);

	public SetConfigS2CPacket(ConfigClassHandler<VolareServerConfig> config) {
		this(NetworkConfigSerializer.write(config));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(MinecraftClient mc) {
		var config = VolareServerConfig.getHandler(mc.world);
		NetworkConfigSerializer.read(serializedConfig, config);
	}
}
