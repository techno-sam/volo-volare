package io.github.slimeistdev.volare.mixin.client;

import io.github.slimeistdev.volare.config.VolareClientConfig;
import io.github.slimeistdev.volare.infrastructure.QuatEntity;
import io.github.slimeistdev.volare.registry.client.VolareHudTextures;
import io.github.slimeistdev.volare.util.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.util.math.MathHelper.RADIANS_PER_DEGREE;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(
		method = "renderCrosshair",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/gui/DrawContext;createNewRootLayer()V",
				ordinal = 0
			),
			to = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/option/GameOptions;getAttackIndicator()Lnet/minecraft/client/option/SimpleOption;",
				ordinal = 0
			)
		)
	)
	private void renderRollIndicator(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		Entity vehicle;
		if (client.player != null && (vehicle = client.player.getRootVehicle()) instanceof QuatEntity quatEntity) {
			var ms = context.getMatrices();
			float tickProgress = tickCounter.getTickProgress(false);

			var config = VolareClientConfig.get();
			boolean rollCamera = client.options.getPerspective().isFirstPerson() ? config.rollCamera1stPerson : config.rollCamera3rdPerson;

			if (rollCamera) {
				var tex = VolareHudTextures.ROLL_INDICATOR;

				ms.pushMatrix();

				//noinspection IntegerDivisionInFloatingPointContext
				ms.rotateAbout(
					quatEntity.getRoll(tickProgress) * RADIANS_PER_DEGREE,
					context.getScaledWindowWidth() / 2,
					context.getScaledWindowHeight() / 2
				);
				context.drawGuiTexture(
					RenderPipelines.GUI_TEXTURED,
					tex.id,
					(context.getScaledWindowWidth() - tex.width) / 2,
					(context.getScaledWindowHeight() - tex.height) / 2,
					tex.width,
					tex.height
				);

				ms.popMatrix();
			}

			{
				context.createNewRootLayer();
				ms.pushMatrix();

				EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
				var renderer = dispatcher.getRenderer(vehicle);
				var state = renderer.getAndUpdateRenderState(vehicle, tickProgress);
				state.hitbox = null;
				if (state instanceof QuatEntity.RenderState quatState)
					quatState.setQuat(new Quaternionf());

				int gliderWidth = 60;
				int gliderHeight = 60;

				int x1 = (context.getScaledWindowWidth() - gliderWidth) / 2;
				int y1 = (context.getScaledWindowHeight() - gliderHeight) / 2 + 32 + 7;

				int x2 = x1 + gliderWidth;
				int y2 = y1 + gliderHeight;

				context.fill(x1 + 2, y1, x2 - 2, y2, 0x40000000);
				context.fill(x1, y1 + 2, x2, y2 - 2, 0x40000000);

				context.addEntity(
					state,
					12.0f,
					new Vector3f(0.0f, 0.0f, 0.0f),
					new MathUtil.EulerAngles(
						0,
						vehicle.getPitch(tickProgress),
						180 + (rollCamera ? 0 : quatEntity.getRoll(tickProgress))
					).getQuat(),
					new Quaternionf(),
					x1,
					y1,
					x2,
					y2
				);
			}
		}
	}
}
