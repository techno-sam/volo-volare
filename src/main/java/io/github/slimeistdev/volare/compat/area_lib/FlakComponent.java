package io.github.slimeistdev.volare.compat.area_lib;

import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.NbtCompound;

public enum FlakComponent implements AreaDataComponent {
	INSTANCE;

	public static FlakComponent instance() {
		return INSTANCE;
	}

	@Override
	public void load(AreaSavedData savedData, NbtCompound tag) {}

	@Override
	public NbtCompound save() {
		return new NbtCompound();
	}
}
