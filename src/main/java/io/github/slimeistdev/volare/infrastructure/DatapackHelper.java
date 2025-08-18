package io.github.slimeistdev.volare.infrastructure;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.slimeistdev.volare.Volare;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.StrictJsonParser;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DatapackHelper {
	public static <T extends Stackable<T>> void loadStacked(ResourceManager manager, ResourceFinder finder, DynamicOps<JsonElement> ops, MapCodec<T> codec, Map<Identifier, T> results) {
		loadStacked(manager, finder, ops, codec, results, Volare.LOG);
	}

	public static <T extends Stackable<T>> void loadStacked(ResourceManager manager, ResourceFinder finder, DynamicOps<JsonElement> ops, MapCodec<T> codec, Map<Identifier, T> results, Logger logger) {
		Codec<StackedData<T>> fullCodec = StackedData.codec(codec);

		List<T> data = new ArrayList<>();

		for (Map.Entry<Identifier, List<Resource>> entry : finder.findAllResources(manager).entrySet()) {
			Identifier fileId = entry.getKey();
			Identifier resourceId = finder.toResourceId(fileId);

			for (Resource resource : entry.getValue()) {
				try {
					Reader reader = resource.getReader();

					try {
						fullCodec.parse(ops, StrictJsonParser.parse(reader)).ifSuccess(value -> {
							if (value.replace) {
								data.clear();
							}

							data.add(value.wrapped);
						}).ifError(error -> logger.error("Couldn't parse data file '{}' from '{}': {}", resourceId, fileId, error));
					} catch (Throwable e) {
						if (reader != null) {
							try {
								reader.close();
							} catch (Throwable e2) {
								e.addSuppressed(e2);
							}
						}

						throw e;
					}

					//noinspection ConstantValue
					if (reader != null) {
						reader.close();
					}
				} catch (IllegalArgumentException | IOException | JsonParseException e) {
					logger.error("Couldn't parse data file '{}' from '{}'", resourceId, fileId, e);
				}
			}

			Iterator<T> iterator = data.iterator();
			if (iterator.hasNext()) {
				T value = iterator.next();

				if (iterator.hasNext()) {
					value = value.stackOver(iterator);
				}

				results.put(resourceId, value);
			}

			data.clear();
		}
	}

	private record StackedData<T extends Stackable<T>>(T wrapped, boolean replace) {
		public static <T extends Stackable<T>> Codec<StackedData<T>> codec(MapCodec<T> wrappedCodec) {
			return RecordCodecBuilder.create(i -> i.group(
				wrappedCodec.forGetter(StackedData::wrapped),
				Codec.BOOL.optionalFieldOf("replace", false).forGetter(StackedData::replace)
			).apply(i, StackedData::new));
		}
	}
}
