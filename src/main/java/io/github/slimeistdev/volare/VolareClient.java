package io.github.slimeistdev.volare;

import io.github.slimeistdev.volare.config.VolareClientConfig;
import io.github.slimeistdev.volare.network.VolarePackets;
import net.fabricmc.api.ClientModInitializer;

public class VolareClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (!VolareClientConfig.load())
			Volare.LOG.error("Error loading Volare client config!");

		ModSetupClient.clientInit();
		VolarePackets.PACKETS.initClient();
	}
}
