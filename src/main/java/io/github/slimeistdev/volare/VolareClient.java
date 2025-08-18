package io.github.slimeistdev.volare;

import io.github.slimeistdev.volare.config.VolareClientConfig;
import io.github.slimeistdev.volare.infrastructure.DynamicItemGroups;
import io.github.slimeistdev.volare.network.VolarePackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class VolareClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (!VolareClientConfig.load())
			Volare.LOG.error("Error loading Volare client config!");

		ModSetupClient.clientInit();
		VolarePackets.PACKETS.initClient();

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new DynamicItemGroups.ReloadListener());
	}
}
