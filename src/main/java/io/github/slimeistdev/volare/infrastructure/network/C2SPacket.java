package io.github.slimeistdev.volare.infrastructure.network;

import net.minecraft.server.network.ServerPlayerEntity;

public interface C2SPacket {
    void handle(ServerPlayerEntity sender);
}
