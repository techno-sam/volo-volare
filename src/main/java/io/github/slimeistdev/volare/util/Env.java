package io.github.slimeistdev.volare.util;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.Supplier;

public enum Env {
	CLIENT, SERVER;

	public static final Env CURRENT = getCurrent();

	public boolean isCurrent() {
		return this == CURRENT;
	}

	public void runIfCurrent(Supplier<Runnable> run) {
		if (isCurrent())
			run.get().run();
	}

	public static <T> T unsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
		return switch (Env.CURRENT) {
			case CLIENT -> clientTarget.get().get();
			case SERVER -> serverTarget.get().get();
		};
	}

	@Internal
	private static Env getCurrent() {
		return switch (FabricLoader.getInstance().getEnvironmentType()) {
			case CLIENT -> CLIENT;
			case SERVER -> SERVER;
		};
	}
}
