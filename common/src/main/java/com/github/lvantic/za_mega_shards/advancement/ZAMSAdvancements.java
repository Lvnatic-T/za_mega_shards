package com.github.lvantic.za_mega_shards.advancement;

import com.github.lvantic.za_mega_shards.ZAMegaShards;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.github.lvantic.za_mega_shards.item.ZAMSItems;
import net.minecraft.world.item.ItemStack;


public final class ZAMSAdvancements {

    private static final String[] DISASSEMBLY_TIERS = {
            "small_sacrifice",
            "second_thoughts",
            "third_times_the_harm",
            "a_costly_experiment",
            "no_refunds"
    };

    private ZAMSAdvancements() {
    }

    public static void awardDisassembly(ServerPlayer player, int tier) {
        award(player, "nothing_goes_to_waste");
        if (tier >= 1 && tier <= DISASSEMBLY_TIERS.length) award(player, DISASSEMBLY_TIERS[tier - 1]);
    }

    public static void checkMassProduction(ServerPlayer player) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ZAMegaShards.MOD_ID, "mass_production");
        AdvancementHolder advancement = player.server.getAdvancements().get(id);
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) return;

        int shardCount = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ZAMSItems.MEGA_SHARD.get())) shardCount += stack.getCount();
            if (shardCount >= 128) {
                player.getAdvancements().award(advancement, "hold_128_shards");
                return;
            }
        }
    }

    private static void award(ServerPlayer player, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ZAMegaShards.MOD_ID, name);
        AdvancementHolder advancement = player.server.getAdvancements().get(id);
        if (advancement != null) player.getAdvancements().award(advancement, "disassemble");
    }
}