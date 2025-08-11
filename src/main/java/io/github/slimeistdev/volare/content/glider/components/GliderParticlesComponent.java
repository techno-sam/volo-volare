package io.github.slimeistdev.volare.content.glider.components;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.List;

public record GliderParticlesComponent(List<ParticleEffect> particles) {
	public static final Codec<GliderParticlesComponent> CODEC = ParticleTypes.TYPE_CODEC
		.listOf()
		.xmap(GliderParticlesComponent::new, GliderParticlesComponent::particles);

	public static final PacketCodec<RegistryByteBuf, GliderParticlesComponent> PACKET_CODEC = ParticleTypes.PACKET_CODEC
		.collect(PacketCodecs.toList())
		.xmap(GliderParticlesComponent::new, GliderParticlesComponent::particles);

	public GliderParticlesComponent(List<ParticleEffect> particles) {
		this.particles = List.copyOf(particles);
	}
}
