package io.github.slimeistdev.volare.content.glider.components;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record GliderParticlesComponent(List<ParticleEffect> particles) implements TooltipAppender {
	public static final Codec<GliderParticlesComponent> CODEC = ParticleTypes.TYPE_CODEC
		.listOf()
		.xmap(GliderParticlesComponent::new, GliderParticlesComponent::particles);

	public static final PacketCodec<RegistryByteBuf, GliderParticlesComponent> PACKET_CODEC = ParticleTypes.PACKET_CODEC
		.collect(PacketCodecs.toList())
		.xmap(GliderParticlesComponent::new, GliderParticlesComponent::particles);

	public GliderParticlesComponent() {
		this(List.of());
	}

	public GliderParticlesComponent(List<ParticleEffect> particles) {
		this.particles = List.copyOf(particles);
	}

	public boolean hasParticle(ParticleEffect particle) {
		return particles.contains(particle);
	}

	public GliderParticlesComponent addParticle(ParticleEffect particle) {
		if (particles.contains(particle)) {
			return this;
		}

		List<ParticleEffect> tmp = new ArrayList<>(particles);
		tmp.add(particle);

		return new GliderParticlesComponent(tmp);
	}

	@Override
	public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
		if (particles.isEmpty())
			return;

		textConsumer.accept(Text.translatable("tooltip.volare.glider_particles").formatted(Formatting.GRAY));

		for (ParticleEffect particle : particles) {
			Registries.PARTICLE_TYPE.getEntry(particle.getType()).getKey().ifPresent(key -> {
				Identifier id = key.getValue();
				textConsumer.accept(Texts.setStyleIfAbsent(
					ScreenTexts.space().append(Text.translatable("volare.particle."+id.toTranslationKey())),
					Style.EMPTY.withColor(Formatting.GRAY)
				));
			});
		}
	}
}
