package io.github.slimeistdev.volare.content.glider.components;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public record GliderFrozenMotionComponent(Vector3fc frozenMotion) implements TooltipAppender {
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

	@Override
	public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
		if (frozenMotion.lengthSquared() < 0.0001f) {
			textConsumer.accept(Text.translatable("tooltip.volare.frozen_motion.zero").formatted(Formatting.GRAY));
		} else {
			textConsumer.accept(Text.translatable("tooltip.volare.frozen_motion",
				String.format("%.2f", frozenMotion.x()),
				String.format("%.2f", frozenMotion.y()),
				String.format("%.2f", frozenMotion.z())
			).formatted(Formatting.GRAY));
		}
	}
}
