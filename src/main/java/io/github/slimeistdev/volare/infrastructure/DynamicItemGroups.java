package io.github.slimeistdev.volare.infrastructure;

import com.mojang.serialization.JsonOps;
import io.github.slimeistdev.volare.Volare;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class DynamicItemGroups {
	private static final Map<Identifier, ItemGroupData> ITEM_GROUPS = new HashMap<>();
	private static boolean dirty = false;

	@ApiStatus.Internal
	public static boolean checkAndClean() {
		boolean wasDirty = dirty;
		dirty = false;
		return wasDirty;
	}

	public static void register(RegistryKey<ItemGroup> key) {
		ItemGroupEvents.modifyEntriesEvent(key).register(group -> fillItems(key.getValue(), group));
	}

	public static void fillItems(Identifier id, ItemGroup.Entries group) {
		ItemGroupData data = ITEM_GROUPS.get(id);
		if (data != null) {
			data.fillItems(group);
		}
	}

	public static class ReloadListener extends SinglePreparationResourceReloader<Map<Identifier, ItemGroupData>> implements IdentifiableResourceReloadListener {
		private static final ResourceFinder FINDER = ResourceFinder.json("volare_item_groups");

		public ReloadListener() {}

		@Override
		public Identifier getFabricId() {
			return Volare.id("item_groups");
		}

		@Override
		protected Map<Identifier, ItemGroupData> prepare(ResourceManager manager, Profiler profiler) {
			Map<Identifier, ItemGroupData> prepared = new HashMap<>();
			DatapackHelper.loadStacked(manager, FINDER, JsonOps.INSTANCE, ItemGroupData.MAP_CODEC, prepared);
			return prepared;
		}

		@Override
		protected void apply(Map<Identifier, ItemGroupData> prepared, ResourceManager manager, Profiler profiler) {
			ITEM_GROUPS.clear();
			ITEM_GROUPS.putAll(prepared);
			dirty = true;
			Volare.LOG.info("Loaded {} item groups", ITEM_GROUPS.size());
		}
	}
}
