package io.github.slimeistdev.volare.content.glider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;
import java.util.function.Predicate;

public record ShowcaseConfig(boolean canMount, boolean shouldTick) implements TooltipAppender {
	public static final ShowcaseConfig DEFAULT = new ShowcaseConfig();

	public static final Codec<ShowcaseConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.BOOL.lenientOptionalFieldOf("can_mount", DEFAULT.canMount).forGetter(ShowcaseConfig::canMount),
		Codec.BOOL.lenientOptionalFieldOf("should_tick", DEFAULT.shouldTick).forGetter(ShowcaseConfig::shouldTick)
	).apply(i, ShowcaseConfig::new));

	public static final PacketCodec<ByteBuf, ShowcaseConfig> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.BOOLEAN,
		ShowcaseConfig::canMount,
		PacketCodecs.BOOLEAN,
		ShowcaseConfig::shouldTick,
		ShowcaseConfig::new
	);

	public ShowcaseConfig() {
		this(true, true);
	}

	@Override
	public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
		appendTooltip(textConsumer, "can_mount", ShowcaseConfig::canMount);
		appendTooltip(textConsumer, "should_tick", ShowcaseConfig::shouldTick);
	}

	private void appendTooltip(Consumer<Text> textConsumer, String key, Predicate<ShowcaseConfig> valueSupplier) {
		boolean value = valueSupplier.test(this);
		boolean defaultValue = valueSupplier.test(DEFAULT);
		if (value == defaultValue) return;

		textConsumer.accept(Texts.setStyleIfAbsent(
			Text.translatable("tooltip.volare.showcase." + key + (value ? ".true" : ".false")),
			Style.EMPTY.withColor(Formatting.GRAY)
		));
	}
}
