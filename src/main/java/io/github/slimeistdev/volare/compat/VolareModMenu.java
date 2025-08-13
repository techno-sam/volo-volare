package io.github.slimeistdev.volare.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.slimeistdev.volare.config.VolareConfigHomeScreen;

public class VolareModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return VolareConfigHomeScreen::new;
	}
}
