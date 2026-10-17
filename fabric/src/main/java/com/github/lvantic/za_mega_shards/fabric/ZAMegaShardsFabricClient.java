package com.github.lvantic.za_mega_shards.fabric;

import com.github.lvantic.za_mega_shards.block.ZAMSBlockEntities;
import com.github.lvantic.za_mega_shards.block.ZAMSBlocks;
import com.github.lvantic.za_mega_shards.client.renderer.MegaResearchStationRenderer;
import com.github.lvantic.za_mega_shards.screen.ZAMSMenuTypes;
import com.github.lvantic.za_mega_shards.screen.custom.screen.MegaResearchStationScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class ZAMegaShardsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(
                ZAMSMenuTypes.MEGA_RESEARCH_STATION.get(),
                MegaResearchStationScreen::new
        );

        BlockEntityRenderers.register(
                ZAMSBlockEntities.MEGA_RESEARCH_STATION.get(),
                MegaResearchStationRenderer::new
        );

        BlockRenderLayerMap.INSTANCE.putBlocks(
                RenderType.cutout(),
                ZAMSBlocks.SMALL_MEGA_ENERGY_BUD.get(),
                ZAMSBlocks.MEDIUM_MEGA_ENERGY_BUD.get(),
                ZAMSBlocks.LARGE_MEGA_ENERGY_BUD.get(),
                ZAMSBlocks.MEGA_ENERGY_CLUSTER.get()
        );
    }
}