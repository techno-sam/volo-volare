package io.github.slimeistdev.volare.mixin.client.yacl;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.FieldAccess;
import dev.isxander.yacl3.gui.YACLScreen;
import io.github.slimeistdev.volare.Volare;
import io.github.slimeistdev.volare.config.VolareServerConfig;
import io.github.slimeistdev.volare.mixin_interfaces.YetAnotherConfigLibImplDuck;
import io.github.slimeistdev.volare.network.VolarePackets;
import io.github.slimeistdev.volare.network.c2s.ConfigChangeC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(YACLScreen.class)
public class YACLScreenMixin {
	@Shadow
	@Final
	public YetAnotherConfigLib config;

	@Inject(method = "finishOrSave", at = @At("HEAD"), remap = false)
	private void backupCurrentConfig(CallbackInfo ci, @Share("oldConfig") LocalRef<VolareServerConfig> oldConfig) {
		if (!((YetAnotherConfigLibImplDuck) config).volare$isServerSided()) return;

		ConfigClassHandler<?> handler = ((YetAnotherConfigLibImplDuck) config).volare$getHandler();
		oldConfig.set(((VolareServerConfig) handler.instance()).copy());
	}

	@Inject(method = "finishOrSave", at = @At(value = "INVOKE", target = "Ldev/isxander/yacl3/api/YetAnotherConfigLib;saveFunction()Ljava/lang/Runnable;"), remap = false)
	private void sendChanges(CallbackInfo ci, @Share("oldConfig") LocalRef<VolareServerConfig> oldConfigRef) {
		if (!((YetAnotherConfigLibImplDuck) config).volare$isServerSided()) return;

		ConfigClassHandler<?> handler = ((YetAnotherConfigLibImplDuck) config).volare$getHandler();
		VolareServerConfig oldConfig = oldConfigRef.get();

		for (int i = 0; i < handler.fields().length; i++) {
			FieldAccess<?> access = handler.fields()[i].access();

			Field backingField;
			try {
				backingField = oldConfig.getClass().getDeclaredField(access.name());
			} catch (NoSuchFieldException e) {
				Volare.LOG.error("Failed to find backing field for config field {}", access.name(), e);
				continue;
			}

			try {
				Object oldValue = backingField.get(oldConfig);
				Object newValue = access.get();
				if (!oldValue.equals(newValue)) {
					Volare.LOG.info("Sending change for field {} from {} to {}", access.name(), oldValue, newValue);
					// send change
					VolarePackets.PACKETS.send(new ConfigChangeC2SPacket(i, access));
				}
			} catch (IllegalAccessException e) {
				Volare.LOG.error("Failed to access backing field for config field {}", access.name(), e);
			}
		}
	}

	@SuppressWarnings("unused")
	@Mixin(YACLScreen.CategoryTab.class)
	private static class CategoryTabMixin {
		@Shadow
		@Final
		public ButtonWidget cancelResetButton;

		@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ldev/isxander/yacl3/gui/YACLScreen$CategoryTab;updateButtons()V"), remap = false)
		private void disableAsNeeded(YACLScreen screen, ConfigCategory category, ScreenRect tabArea, CallbackInfo ci) {
			if (!(screen.config instanceof YetAnotherConfigLibImplDuck serverConfigLib)) return;
			if (!serverConfigLib.volare$isServerSided()) return;

			var mc = MinecraftClient.getInstance();
			if (mc.player == null) {
				cancelResetButton.active = false;
				return;
			}

			if (!VolareServerConfig.isAuthorizedToChange(mc.player)) {
				cancelResetButton.active = false;
			}
		}
	}
}
