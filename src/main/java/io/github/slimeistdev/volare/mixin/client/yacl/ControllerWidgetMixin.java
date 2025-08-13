package io.github.slimeistdev.volare.mixin.client.yacl;

import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import io.github.slimeistdev.volare.config.VolareServerConfig;
import io.github.slimeistdev.volare.mixin_interfaces.YetAnotherConfigLibImplDuck;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ControllerWidget.class)
public class ControllerWidgetMixin {
	@Shadow
	@Final
	protected YACLScreen screen;

	@Inject(method = "isAvailable", at = @At("HEAD"), cancellable = true, remap = false)
	private void disableAvailabilityIfNotOp(CallbackInfoReturnable<Boolean> cir) {
		if (!(screen.config instanceof YetAnotherConfigLibImplDuck serverConfigLib)) return;
		if (!serverConfigLib.volare$isServerSided()) return;

		var mc = MinecraftClient.getInstance();
		if (mc.player == null) {
			cir.setReturnValue(false);
			return;
		}

		if (!VolareServerConfig.isAuthorizedToChange(mc.player)) {
			cir.setReturnValue(false);
		}
	}
}
