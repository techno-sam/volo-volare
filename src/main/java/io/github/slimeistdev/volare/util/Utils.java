package io.github.slimeistdev.volare.util;

import net.fabricmc.loader.api.FabricLoader;

public class Utils {
	public static boolean isDevEnv() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
}
