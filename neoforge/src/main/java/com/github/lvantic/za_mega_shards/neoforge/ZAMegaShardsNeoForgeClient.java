package com.github.lvantic.za_mega_shards.neoforge;

import com.github.lvantic.za_mega_shards.ZAMegaShards;
import com.github.lvantic.za_mega_shards.block.ZAMSBlockEntities;
import com.github.lvantic.za_mega_shards.client.renderer.MegaResearchStationRenderer;
import com.github.lvantic.za_mega_shards.screen.ZAMSMenuTypes;
import com.github.lvantic.za_mega_shards.screen.custom.screen.MegaResearchStationScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = ZAMegaShards.MOD_ID, value = Dist.CLIENT)
public final class ZAMegaShardsNeoForgeClient {

    private ZAMegaShardsNeoForgeClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ZAMSMenuTypes.MEGA_RESEARCH_STATION.get(), MegaResearchStationScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ZAMSBlockEntities.MEGA_RESEARCH_STATION.get(),
                MegaResearchStationRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerResourcePacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(ZAMegaShards.MOD_ID, "resourcepacks/dark_mode"),
                PackType.CLIENT_RESOURCES,
                Component.literal("ZA Mega Shards - Dark Mode"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP
        );
    }
}