package com.github.lvantic.za_mega_shards.fabric;

import com.github.lvantic.za_mega_shards.ZAMegaShards;
import com.github.lvantic.za_mega_shards.fabric.villager.ZAMSPointOfInterestTypesFabric;
import com.github.lvantic.za_mega_shards.villager.ZAMSVillagerTrades;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ZAMegaShardsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ZAMegaShards.init();
        ZAMSPointOfInterestTypesFabric.register();
        ZAMSVillagerTrades.register();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    ResourceLocation.fromNamespaceAndPath(ZAMegaShards.MOD_ID, "dark_mode"),
                    FabricLoader.getInstance().getModContainer(ZAMegaShards.MOD_ID).orElseThrow(),
                    Component.literal("ZA Mega Shards - Dark Mode"),
                    ResourcePackActivationType.NORMAL
            );
        }
    }
}