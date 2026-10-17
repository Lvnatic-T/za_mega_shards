
package com.github.lvantic.za_mega_shards.worldgen.feature;

import com.github.lvantic.za_mega_shards.block.ZAMSBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MegaEnergyDepositFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_Y = -58;
    private static final int MAX_Y = 48;
    private static final int SEARCH_ATTEMPTS = 128;

    public MegaEnergyDepositFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ChunkPos chunk = new ChunkPos(context.origin());

        int worldMinY = Math.max(MIN_Y, level.getMinBuildHeight() + 1);
        int worldMaxY = Math.min(MAX_Y, level.getMaxBuildHeight() - 2);

        if (worldMaxY < worldMinY) return false;

        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            int y = worldMinY + random.nextInt(worldMaxY - worldMinY + 1);

            BlockPos candidate = new BlockPos(x, y, z);
            if (!isValidCorePosition(level, candidate)) continue;

            int targetSize = 1 + random.nextInt(3);
            placeDeposit(level, chunk, candidate, targetSize, random);
            return true;
        }

        return false;
    }

    private void placeDeposit(WorldGenLevel level, ChunkPos chunk, BlockPos firstPos, int targetSize, RandomSource random) {
        List<BlockPos> placed = new ArrayList<>();
        placed.add(firstPos);
        placeCore(level, firstPos);

        while (placed.size() < targetSize) {
            List<BlockPos> candidates = new ArrayList<>();
            Set<BlockPos> checked = new HashSet<>();

            for (BlockPos placedPos : placed) {
                for (Direction direction : Direction.values()) {
                    BlockPos candidate = placedPos.relative(direction);

                    if (!new ChunkPos(candidate).equals(chunk)) continue;
                    if (placed.contains(candidate)) continue;
                    if (!checked.add(candidate)) continue;

                    if (isValidCorePosition(level, candidate)) {
                        candidates.add(candidate);
                    }
                }
            }

            if (candidates.isEmpty()) break;

            BlockPos selected = candidates.get(random.nextInt(candidates.size()));
            placeCore(level, selected);
            placed.add(selected);
        }
    }

    private void placeCore(WorldGenLevel level, BlockPos pos) {
        BlockState replacedState = level.getBlockState(pos);

        if (replacedState.is(Blocks.DEEPSLATE)) {
            BlockState coreState = ZAMSBlocks.DEEPSLATE_MEGA_ENERGY_CORE.get().defaultBlockState();

            if (replacedState.hasProperty(RotatedPillarBlock.AXIS) && coreState.hasProperty(RotatedPillarBlock.AXIS)) {
                coreState = coreState.setValue(RotatedPillarBlock.AXIS, replacedState.getValue(RotatedPillarBlock.AXIS));
            }

            this.setBlock(level, pos, coreState);
            return;
        }

        this.setBlock(level, pos, ZAMSBlocks.MEGA_ENERGY_CORE.get().defaultBlockState());
    }

    private boolean isValidCorePosition(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (!state.is(Blocks.STONE) && !state.is(Blocks.DEEPSLATE)) return false;

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        if (pos.getY() >= surfaceY - 4) return false;

        for (Direction direction : Direction.values()) {
            if (isCaveSpace(level.getBlockState(pos.relative(direction)))) {
                return true;
            }
        }

        return false;
    }

    private boolean isCaveSpace(BlockState state) {
        return state.isAir() || state.getFluidState().is(FluidTags.WATER);
    }
}
