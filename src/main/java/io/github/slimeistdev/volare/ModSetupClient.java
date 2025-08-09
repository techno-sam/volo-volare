package io.github.slimeistdev.volare;

import io.github.slimeistdev.volare.registry.client.VolareEntityRenderers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModSetupClient {
	public static void clientInit() {
		VolareEntityRenderers.clientInit();
	}
}
