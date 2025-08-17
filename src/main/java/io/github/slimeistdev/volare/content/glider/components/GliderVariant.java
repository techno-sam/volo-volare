package io.github.slimeistdev.volare.content.glider.components;

import com.mojang.serialization.Codec;
import io.github.slimeistdev.volare.Volare;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public record GliderVariant(AssetInfo assetInfo) implements TooltipAppender {
	public static final Codec<GliderVariant> CODEC = AssetInfo.CODEC.xmap(GliderVariant::new, GliderVariant::assetInfo);
	public static final PacketCodec<ByteBuf, GliderVariant> PACKET_CODEC = AssetInfo.PACKET_CODEC.xmap(GliderVariant::new, GliderVariant::assetInfo);

	public static final GliderVariant DEFAULT = new GliderVariant(new AssetInfo(Volare.id("entity/glider/colorful")));

	public String getTranslationKey() {
		Identifier id = assetInfo.id();
		return "entity.volare.glider.variant." + id.getNamespace() + '.' + id.getPath().replace('/', '.');
	}

	@Override
	public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
		textConsumer.accept(Texts.setStyleIfAbsent(
			Text.translatable(getTranslationKey()),
			Style.EMPTY.withColor(Formatting.GRAY)
		));
	}
}
