package com.github.lvantic.za_mega_shards.neoforge;

import com.github.lvantic.za_mega_shards.ZAMegaShards;
import com.github.lvantic.za_mega_shards.neoforge.villager.ZAMSPointOfInterestTypesNeoForge;
import com.github.lvantic.za_mega_shards.villager.ZAMSVillagerTrades;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(ZAMegaShards.MOD_ID)
public final class ZAMegaShardsNeoForge {

    public ZAMegaShardsNeoForge(IEventBus modEventBus, ModContainer container) {
        ZAMSPointOfInterestTypesNeoForge.register(modEventBus);
        ZAMegaShards.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ZAMegaShardsNeoForgeClient.registerConfigScreen(container);
        }

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ZAMSVillagerTrades::register);
    }
}