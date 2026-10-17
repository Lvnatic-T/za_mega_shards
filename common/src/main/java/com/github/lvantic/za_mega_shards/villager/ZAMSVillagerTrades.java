package com.github.lvantic.za_mega_shards.villager;

import com.github.lvantic.za_mega_shards.config.ZAMSConfig;
import com.github.lvantic.za_mega_shards.tag.ZAMSItemTags;
import com.github.lvantic.za_mega_shards.villager.trade.RandomMegaStoneTrade;
import dev.architectury.registry.level.entity.trade.TradeRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ZAMSVillagerTrades {

    private static final int NOVICE_XP = 2;
    private static final int APPRENTICE_XP = 10;
    private static final int JOURNEYMAN_XP = 20;
    private static final int EXPERT_XP = 30;
    private static final int MASTER_XP = 30;

    private static boolean registered = false;

    private ZAMSVillagerTrades() {
    }

    public static void register() {
        if (registered) return;
        registered = true;

        int maxUses = ZAMSConfig.get().getMaxTradesPerOffer();

        registerTier(1, ZAMSItemTags.MEGA_STONE_TIER_1, NOVICE_XP, maxUses);
        registerTier(2, ZAMSItemTags.MEGA_STONE_TIER_2, APPRENTICE_XP, maxUses);
        registerTier(3, ZAMSItemTags.MEGA_STONE_TIER_3, JOURNEYMAN_XP, maxUses);
        registerTier(4, ZAMSItemTags.MEGA_STONE_TIER_4, EXPERT_XP, maxUses);

        TradeRegistry.registerVillagerTrade(
                ZAMSVillagerProfessions.MEGA_RESEARCHER.get(),
                5,
                new RandomMegaStoneTrade(ZAMSItemTags.MEGA_STONE_TIER_5, maxUses, MASTER_XP, false)
        );
    }

    private static void registerTier(int villagerLevel, TagKey<Item> tierTag, int villagerXp, int maxUses) {
        TradeRegistry.registerVillagerTrade(
                ZAMSVillagerProfessions.MEGA_RESEARCHER.get(),
                villagerLevel,
                new RandomMegaStoneTrade(tierTag, maxUses, villagerXp, true),
                new RandomMegaStoneTrade(tierTag, maxUses, villagerXp, false)
        );
    }
}