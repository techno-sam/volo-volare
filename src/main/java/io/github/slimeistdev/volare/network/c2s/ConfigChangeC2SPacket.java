package io.github.slimeistdev.volare.network.c2s;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.FieldAccess;
import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.config.VolareServerConfig;
import io.github.slimeistdev.volare.config.serializers.NetworkConfigSerializer;
import io.github.slimeistdev.volare.infrastructure.network.C2SPacket;
import io.github.slimeistdev.volare.util.VolarePacketCodecs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public record ConfigChangeC2SPacket(int idx, String name, PacketByteBuf serializedField) implements C2SPacket {
    public static final PacketCodec<PacketByteBuf, ConfigChangeC2SPacket> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
		ConfigChangeC2SPacket::idx,
        PacketCodecs.string(PacketByteBuf.DEFAULT_MAX_STRING_LENGTH),
		ConfigChangeC2SPacket::name,
        VolarePacketCodecs.NESTED_BUF_PACKET_CODEC,
		ConfigChangeC2SPacket::serializedField,
		ConfigChangeC2SPacket::new
    );

    public ConfigChangeC2SPacket(int i, FieldAccess<?> access) {
        this(i, access.name(), NetworkConfigSerializer.write(access));
    }

    @Override
    public void handle(ServerPlayerEntity sender) {
        if (!VolareServerConfig.isAuthorizedToChange(sender)) {
            Volare.LOG.warn("PLayer {} tried to change config without permission", sender);
            sender.networkHandler.disconnect(Text.of("You are not authorized to change Volare config"));
            return;
        }

        ConfigClassHandler<VolareServerConfig> config = VolareServerConfig.getHandler(sender.getWorld());

        FieldAccess<?> access = config.fields()[MathHelper.clamp(idx, 0, config.fields().length-1)].access();

        if (access.name().equals(name)) {
            NetworkConfigSerializer.read(serializedField, access);
            config.save();
            return;
        }

        for (ConfigField<?> field : config.fields()) {
            access = field.access();
            if (access.name().equals(name)) {
                NetworkConfigSerializer.read(serializedField, access);
                config.save();
                return;
            }
        }

        Volare.LOG.warn("Failed to find config field with name {}", name);
    }
}
