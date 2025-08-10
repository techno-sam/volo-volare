package io.github.slimeistdev.volare.content.glider;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.joml.Vector3f;

public record PhysicsSnapshot(Vector3f velocity, Vector3f angularVelocity) {
	public static final PacketCodec<ByteBuf, PhysicsSnapshot> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.VECTOR_3F,
		PhysicsSnapshot::velocity,
		PacketCodecs.VECTOR_3F,
		PhysicsSnapshot::angularVelocity,
		PhysicsSnapshot::new
	);

	public PhysicsSnapshot(RigidBody body) {
		this(new Vector3f(body.getVelocity()), new Vector3f(body.getAngularVelocity()));
	}

	public void applyTo(RigidBody body) {
		body.setVelocity(velocity);
		body.setAngularVelocity(angularVelocity);
	}
}
