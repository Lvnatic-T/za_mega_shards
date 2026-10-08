package com.github.lvantic.za_mega_shards.block;

import com.github.lvantic.za_mega_shards.ZAMegaShards;
import com.github.lvantic.za_mega_shards.block.custom.MegaEnergyCoreBlock;
import com.github.lvantic.za_mega_shards.block.custom.DeepslateMegaEnergyCoreBlock;
import com.github.lvantic.za_mega_shards.block.custom.MegaResearchStationBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class ZAMSBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(
                    ZAMegaShards.MOD_ID,
                    Registries.BLOCK
            );

    public static final RegistrySupplier<Block> MEGA_RESEARCH_STATION =
            BLOCKS.register(
                    "mega_research_station",
                    () -> new MegaResearchStationBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .strength(2.5F)
                                    .noOcclusion()
                                    .sound(SoundType.WOOD)
                    )
            );


    public static final RegistrySupplier<Block> MEGA_SHARD_BLOCK =
            BLOCKS.register(
                    "mega_shard_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_PURPLE)
                                    .strength(3.0F)
                                    .sound(SoundType.AMETHYST)
                    )
            );

    public static final RegistrySupplier<Block> MEGA_ENERGY_CORE =
            BLOCKS.register(
                    "mega_energy_core",
                    () -> new MegaEnergyCoreBlock(
                            BlockBehaviour.Properties
                                    .ofFullCopy(Blocks.STONE)
                                    .randomTicks()
                                    .pushReaction(PushReaction.DESTROY)
                                    .noLootTable()
                    )
            );

    public static final RegistrySupplier<Block> DEEPSLATE_MEGA_ENERGY_CORE =
            BLOCKS.register(
                    "deepslate_mega_energy_core",
                    () -> new DeepslateMegaEnergyCoreBlock(
                            BlockBehaviour.Properties
                                    .ofFullCopy(Blocks.DEEPSLATE)
                                    .randomTicks()
                                    .pushReaction(PushReaction.DESTROY)
                                    .noLootTable()
                    )
            );

    /*
     * Mega Energy growth stages
     */

    public static final RegistrySupplier<Block> SMALL_MEGA_ENERGY_BUD =
            BLOCKS.register(
                    "small_mega_energy_bud",
                    () -> new AmethystClusterBlock(
                            3.0F,
                            4.0F,
                            crystalProperties(
                                    SoundType.SMALL_AMETHYST_BUD,
                                    1
                            )
                    )
            );

    public static final RegistrySupplier<Block> MEDIUM_MEGA_ENERGY_BUD =
            BLOCKS.register(
                    "medium_mega_energy_bud",
                    () -> new AmethystClusterBlock(
                            4.0F,
                            3.0F,
                            crystalProperties(
                                    SoundType.MEDIUM_AMETHYST_BUD,
                                    2
                            )
                    )
            );

    public static final RegistrySupplier<Block> LARGE_MEGA_ENERGY_BUD =
            BLOCKS.register(
                    "large_mega_energy_bud",
                    () -> new AmethystClusterBlock(
                            5.0F,
                            3.0F,
                            crystalProperties(
                                    SoundType.LARGE_AMETHYST_BUD,
                                    4
                            )
                    )
            );

    public static final RegistrySupplier<Block> MEGA_ENERGY_CLUSTER =
            BLOCKS.register(
                    "mega_energy_cluster",
                    () -> new AmethystClusterBlock(
                            7.0F,
                            3.0F,
                            crystalProperties(
                                    SoundType.AMETHYST_CLUSTER,
                                    5
                            )
                    )
            );

    private static BlockBehaviour.Properties crystalProperties(
            SoundType soundType,
            int lightLevel
    ) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .forceSolidOn()
                .noOcclusion()
                .sound(soundType)
                .strength(1.5F)
                .lightLevel(state -> lightLevel)
                .pushReaction(PushReaction.DESTROY);
    }

    public static void register() {
        BLOCKS.register();
    }
}