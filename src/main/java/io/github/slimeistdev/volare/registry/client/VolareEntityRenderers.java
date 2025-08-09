package io.github.slimeistdev.volare.registry.client;

import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.content.glider.render.GliderEntityRenderer;
import io.github.slimeistdev.volare.registry.VolareEntities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;

@Environment(EnvType.CLIENT)
public class VolareEntityRenderers {
	// Doesn't have to be registered, because JsonEM handles it for us!
	public static final EntityModelLayer GLIDER_LAYER = new EntityModelLayer(Volare.id("glider"), "main");

	public static void clientInit() {
		EntityRendererRegistry.register(VolareEntities.GLIDER, GliderEntityRenderer::new);
	}
}
