package io.github.slimeistdev.volare.infrastructure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record ItemGroupData(List<Entry> entries) implements Stackable<ItemGroupData> {
	public static final MapCodec<ItemGroupData> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Entry.CODEC.listOf().fieldOf("entries").forGetter(ItemGroupData::entries)
	).apply(i, ItemGroupData::new));

	public static final Codec<ItemGroupData> CODEC = MAP_CODEC.codec();

	@Override
	public ItemGroupData stackOver(ItemGroupData other) {
		return stackOver(List.of(other).iterator());
	}

	@Override
	public ItemGroupData stackOver(Iterator<ItemGroupData> others) {
		ObjectLinkedOpenHashSet<Entry> entries = new ObjectLinkedOpenHashSet<>();
		entries.addAll(this.entries);
		others.forEachRemaining(other -> entries.addAll(other.entries));

		List<Entry> out = new ArrayList<>();

		Object2IntOpenHashMap<ItemStack> firstAppearances = new Object2IntOpenHashMap<>();
		firstAppearances.defaultReturnValue(-1);

		var iterator = entries.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Entry entry = iterator.next();

			int existingIndex = firstAppearances.putIfAbsent(entry.stack, i);
			if (existingIndex == -1) {
				out.add(entry);
				i++;
			} else {
				Entry existing = out.get(existingIndex);
				ItemGroup.StackVisibility mergedVisibility = mergeVisibility(existing.visibility, entry.visibility);
				out.set(existingIndex, new Entry(existing.stack, mergedVisibility));
			}
		}

		return new ItemGroupData(List.copyOf(entries));
	}

	public void fillItems(ItemGroup.Entries group) {
		for (Entry entry : entries) {
			group.add(entry.stack, entry.visibility);
		}
	}

	private static ItemGroup.StackVisibility mergeVisibility(ItemGroup.StackVisibility a, ItemGroup.StackVisibility b) {
		return a == b ? a : ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS;
	}

	public record Entry(ItemStack stack, ItemGroup.StackVisibility visibility) {
		private static final Codec<ItemGroup.StackVisibility> STACK_VISIBILITY_CODEC = Codec.stringResolver(
			v -> switch (v) {
				case PARENT_AND_SEARCH_TABS -> "always";
				case PARENT_TAB_ONLY -> "parent_tab_only";
				case SEARCH_TAB_ONLY -> "search_tab_only";
			},
			s -> switch (s) {
				case "always" -> ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS;
				case "parent_tab_only" -> ItemGroup.StackVisibility.PARENT_TAB_ONLY;
				case "search_tab_only" -> ItemGroup.StackVisibility.SEARCH_TAB_ONLY;
				default -> null;
			}
		);

		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
			ItemStack.MAP_CODEC.forGetter(Entry::stack),
			STACK_VISIBILITY_CODEC.optionalFieldOf("visibility", ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS).forGetter(Entry::visibility)
		).apply(i, Entry::new));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final List<Entry> entries = new ArrayList<>();

		protected Builder() {}

		public Builder add(ItemConvertible item) {
			return add(item.asItem().getDefaultStack());
		}

		public Builder add(ItemStack stack) {
			return add(stack, ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
		}

		public Builder add(ItemConvertible item, ItemGroup.StackVisibility visibility) {
			return add(item.asItem().getDefaultStack(), visibility);
		}

		public Builder add(ItemStack stack, ItemGroup.StackVisibility visibility) {
			entries.add(new Entry(stack, visibility));
			return this;
		}

		public ItemGroupData build() {
			return new ItemGroupData(List.copyOf(entries));
		}
	}
}
