package io.github.slimeistdev.volare.mixin;

import io.github.slimeistdev.volare.infrastructure.DynamicItemGroups;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroup.DisplayContext.class)
public class DisplayContextMixin {
	@Inject(method = "doesNotMatch", at = @At("HEAD"), cancellable = true)
	private void includeReload(FeatureSet enabledFeatures, boolean hasPermissions, RegistryWrapper.WrapperLookup registries, CallbackInfoReturnable<Boolean> cir) {
		if (DynamicItemGroups.checkAndClean())
			cir.setReturnValue(true);
	}
}
