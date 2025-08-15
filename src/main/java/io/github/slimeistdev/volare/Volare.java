package io.github.slimeistdev.volare;

import io.github.slimeistdev.volare.config.VolareServerConfig;
import io.github.slimeistdev.volare.content.glider.GliderEntity;
import io.github.slimeistdev.volare.network.VolarePackets;
import io.github.slimeistdev.volare.network.s2c.SetConfigS2CPacket;
import io.github.slimeistdev.volare.registry.VolareTags;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
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

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			VolareServerConfig.get(server.getOverworld());
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			VolarePackets.PACKETS.sendTo(player, new SetConfigS2CPacket(VolareServerConfig.getHandler(player.getWorld())));
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!world.isClient && player.getRootVehicle() instanceof GliderEntity glider && stack.isIn(VolareTags.THRUST_SOURCE)) {
				glider.boostThrust();
				stack.decrementUnlessCreative(1, player);
				return ActionResult.SUCCESS_SERVER;
			}

			return ActionResult.PASS;
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}

	public static <T> RegistryKey<T> key(RegistryKey<? extends Registry<T>> registryKey, String path) {
		return RegistryKey.of(registryKey, id(path));
	}
}
