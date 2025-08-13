package io.github.slimeistdev.volare.mixin.client.yacl;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.impl.YetAnotherConfigLibImpl;
import io.github.slimeistdev.volare.mixin_interfaces.YetAnotherConfigLibImplDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(YetAnotherConfigLibImpl.class)
@SuppressWarnings("UnstableApiUsage")
public class YetAnotherConfigLibImplMixin implements YetAnotherConfigLibImplDuck {
	@Unique
	private boolean volare$serverSided = false;

	@Unique
	private ConfigClassHandler<?> volare$handler;

	@Override
	public void volare$setServerSided(boolean serverSided) {
		this.volare$serverSided = serverSided;
	}

	@Override
	public boolean volare$isServerSided() {
		return this.volare$serverSided;
	}

	@Override
	public void volare$setHandler(ConfigClassHandler<?> handler) {
		this.volare$handler = handler;
	}

	@Override
	public ConfigClassHandler<?> volare$getHandler() {
		return this.volare$handler;
	}
}
