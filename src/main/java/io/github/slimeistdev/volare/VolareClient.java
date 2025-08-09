package io.github.slimeistdev.volare;

import io.github.slimeistdev.volare.network.VolarePackets;
import net.fabricmc.api.ClientModInitializer;

public class VolareClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModSetupClient.clientInit();
		VolarePackets.PACKETS.initClient();
	}
}
