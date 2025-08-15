package io.github.slimeistdev.volare.compat.area_lib;

import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class ThermalComponent implements AreaDataComponent {
	private float strength;
	private boolean absolute;

	public ThermalComponent() {
		this(0.0f, true);
	}

	public ThermalComponent(float strength, boolean absolute) {
		this.strength = strength;
		this.absolute = absolute;
	}

	public float getStrength() {
		return strength;
	}

	public boolean isAbsolute() {
		return absolute;
	}

	@Override
	public void load(AreaSavedData savedData, NbtCompound tag) {
		this.strength = tag.getFloat("strength", 0.0f);
		this.absolute = tag.getBoolean("absolute", true);
	}

	@Override
	public NbtCompound save() {
		NbtCompound tag = new NbtCompound();
		tag.putFloat("strength", this.strength);
		tag.putBoolean("absolute", this.absolute);
		return tag;
	}

	public @Nullable Float calculateStrength(float configStrength) {
		return isAbsolute() ? getStrength() : configStrength * getStrength();
	}
}
