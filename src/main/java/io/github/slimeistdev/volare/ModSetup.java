package io.github.slimeistdev.volare;

import io.github.slimeistdev.volare.compat.area_lib.AreaLibProxy;
import io.github.slimeistdev.volare.registry.*;

public class ModSetup {
	public static void init() {
		VolareDataComponentTypes.init();
		VolareEntities.init();
		VolareItems.init();
		VolareSlotDisplays.init();
		VolareRecipeSerializers.init();
		VolareSoundEvents.init();
		AreaLibProxy.init();
		VolareCommands.init();
	}
}
