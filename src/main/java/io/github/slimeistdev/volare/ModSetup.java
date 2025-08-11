package io.github.slimeistdev.volare;

import io.github.slimeistdev.volare.registry.VolareDataComponentTypes;
import io.github.slimeistdev.volare.registry.VolareEntities;
import io.github.slimeistdev.volare.registry.VolareItems;

public class ModSetup {
	public static void init() {
		VolareDataComponentTypes.init();
		VolareEntities.init();
		VolareItems.init();
	}
}
