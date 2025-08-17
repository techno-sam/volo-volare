package io.github.slimeistdev.volare.registry;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.components.GliderVariant;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public class VolareTrackedData {
	public static final TrackedDataHandler<GliderVariant> GLIDER_VARIANT = register("glider/variant", GliderVariant.PACKET_CODEC);

	public static <T> TrackedDataHandler<T> register(String id, PacketCodec<? super RegistryByteBuf, T> codec) {
		TrackedDataHandler<T> handler = TrackedDataHandler.create(codec);
		return register(id, handler);
	}

	public static <T> TrackedDataHandler<T> register(String id, TrackedDataHandler<T> handler) {
		FabricTrackedDataRegistry.register(Volare.id(id), handler);
		return handler;
	}

	public static void init() {}
}
