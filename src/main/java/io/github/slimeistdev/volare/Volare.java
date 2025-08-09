package io.github.slimeistdev.volare;

import io.github.slimeistdev.volare.network.VolarePackets;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Volare implements ModInitializer {
	public static final String ID = "volare";
	public static final Logger LOG = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize() {
		ModSetup.init();
		VolarePackets.PACKETS.initCommon();
	}

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}

	public static <T> RegistryKey<T> key(RegistryKey<? extends Registry<T>> registryKey, String path) {
		return RegistryKey.of(registryKey, id(path));
	}
}
