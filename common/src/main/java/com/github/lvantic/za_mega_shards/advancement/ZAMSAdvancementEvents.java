package com.github.lvantic.za_mega_shards.advancement;

import com.github.lvantic.za_mega_shards.ZAMegaShards;
import com.github.lvantic.za_mega_shards.block.ZAMSBlocks;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class ZAMSAdvancementEvents {

    private static final ResourceLocation WRONG_TOOL_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath(ZAMegaShards.MOD_ID, "shouldnt_have_done_that");

    private ZAMSAdvancementEvents() {
    }

    public static void register() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (player.isCreative()) return EventResult.pass();

            boolean isBud = state.is(ZAMSBlocks.SMALL_MEGA_ENERGY_BUD.get())
                    || state.is(ZAMSBlocks.MEDIUM_MEGA_ENERGY_BUD.get())
                    || state.is(ZAMSBlocks.LARGE_MEGA_ENERGY_BUD.get());

            if (!isBud) return EventResult.pass();

            var silkTouch = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH);
            if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, player.getMainHandItem()) > 0) return EventResult.pass();

            AdvancementHolder advancement = player.server.getAdvancements().get(WRONG_TOOL_ADVANCEMENT);
            if (advancement != null) player.getAdvancements().award(advancement, "break_without_silk_touch");

            return EventResult.pass();
        });

        TickEvent.PLAYER_POST.register(player -> {
            if (player instanceof ServerPlayer serverPlayer) ZAMSAdvancements.checkMassProduction(serverPlayer);
        });
    }
}