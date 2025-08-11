package io.github.slimeistdev.volare.content.glider.components;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record GliderFrozenMotionComponent(Vector3fc frozenMotion) {
	public static final Codec<GliderFrozenMotionComponent> CODEC = Codecs.VECTOR_3F.xmap(
		GliderFrozenMotionComponent::new,
		GliderFrozenMotionComponent::frozenMotionMut
	);

	public static final PacketCodec<ByteBuf, GliderFrozenMotionComponent> PACKET_CODEC = PacketCodecs.VECTOR_3F.xmap(
		GliderFrozenMotionComponent::new,
		GliderFrozenMotionComponent::frozenMotionMut
	);

	public Vector3f frozenMotionMut() {
		return new Vector3f(frozenMotion);
	}
}
