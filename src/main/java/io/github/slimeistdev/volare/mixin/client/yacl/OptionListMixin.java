package io.github.slimeistdev.volare.mixin.client.yacl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import io.github.slimeistdev.volare.config.VolareServerConfig;
import io.github.slimeistdev.volare.mixin_interfaces.YetAnotherConfigLibImplDuck;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(OptionListWidget.class)
public class OptionListMixin {
	@SuppressWarnings("unused")
	@Mixin(OptionListWidget.OptionEntry.class)
	private static class OptionEntryMixin {
		@WrapOperation(
			method = {
				"<init>",
				"lambda$new$1"
			},
			at = @At(
				value = "INVOKE",
				target = "Ldev/isxander/yacl3/api/Option;available()Z"
			),
			remap = false
		)
		private boolean makeUnavailable(Option<?> instance, Operation<Boolean> original) {
			var mc = MinecraftClient.getInstance();
			var screen$ = mc.currentScreen;

			if (!(screen$ instanceof YACLScreen screen)) return original.call(instance);

			if (!(screen.config instanceof YetAnotherConfigLibImplDuck serverConfigLib)) return original.call(instance);
			if (!serverConfigLib.volare$isServerSided()) return original.call(instance);

			if (mc.player == null)
				return false;

			if (!VolareServerConfig.isAuthorizedToChange(mc.player))
				return false;

			return original.call(instance);
		}
	}

	@SuppressWarnings("unused")
	@Mixin(OptionListWidget.ListGroupSeparatorEntry.class)
	private static class ListGroupSeparatorEntryMixin {
		@WrapOperation(
			method = "lambda$new$1",
			at = @At(
				value = "INVOKE",
				target = "Ldev/isxander/yacl3/api/Option;available()Z"
			),
			remap = false
		)
		private boolean makeUnavailable(Option<?> instance, Operation<Boolean> original) {
			return volare$makeUnavailable(instance, original);
		}

		@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Ldev/isxander/yacl3/api/ListOption;available()Z"))
		private boolean makeUnavailable(ListOption<?> instance, Operation<Boolean> original) {
			return volare$makeUnavailable(instance, original);
		}

		@Unique
		private static boolean volare$makeUnavailable(Option<?> instance, Operation<Boolean> original) {
			var mc = MinecraftClient.getInstance();
			var screen$ = mc.currentScreen;

			if (!(screen$ instanceof YACLScreen screen)) return original.call(instance);

			if (!(screen.config instanceof YetAnotherConfigLibImplDuck serverConfigLib)) return original.call(instance);
			if (!serverConfigLib.volare$isServerSided()) return original.call(instance);

			if (mc.player == null)
				return false;

			if (!VolareServerConfig.isAuthorizedToChange(mc.player))
				return false;

			return original.call(instance);
		}
	}
}
