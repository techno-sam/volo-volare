package io.github.slimeistdev.volare.infrastructure;

import org.jetbrains.annotations.ApiStatus;

import java.util.Iterator;

public interface Stackable<Self extends Stackable<Self>> {
	Self stackOver(Self other);

	default Self stackOver(Iterator<Self> others) {
		Self result = cast();
		while (others.hasNext()) {
			result = result.stackOver(others.next());
		}
		return result;
	}

	@ApiStatus.NonExtendable
	@SuppressWarnings("unchecked")
	default Self cast() {
		return (Self) this;
	}
}
