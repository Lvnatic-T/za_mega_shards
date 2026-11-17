package com.github.lvantic.za_mega_shards.fabric;

import com.github.lvantic.za_mega_shards.client.config.ZAMSConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public final class ZAMSModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        boolean clothConfig = FabricLoader.getInstance().isModLoaded("cloth-config2")
                || FabricLoader.getInstance().isModLoaded("cloth-config");

        return clothConfig ? ZAMSConfigScreen::create : null;
    }
}