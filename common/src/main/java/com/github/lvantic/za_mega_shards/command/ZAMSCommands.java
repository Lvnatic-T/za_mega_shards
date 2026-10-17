package com.github.lvantic.za_mega_shards.command;

import com.github.lvantic.za_mega_shards.ZAMegaShards;
import com.github.lvantic.za_mega_shards.config.ZAMSConfig;
import com.github.lvantic.za_mega_shards.tag.ZAMSItemTags;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

public final class ZAMSCommands {

    private static final List<TagKey<Item>> TIER_TAGS = List.of(
            ZAMSItemTags.MEGA_STONE_TIER_1,
            ZAMSItemTags.MEGA_STONE_TIER_2,
            ZAMSItemTags.MEGA_STONE_TIER_3,
            ZAMSItemTags.MEGA_STONE_TIER_4,
            ZAMSItemTags.MEGA_STONE_TIER_5
    );

    private ZAMSCommands() {
    }

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) ->
                dispatcher.register(Commands.literal("za_mega_shards")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("info")
                                .executes(context -> showInfo(context.getSource()))
                        )
                )
        );
    }

    private static int showInfo(CommandSourceStack source) {
        ZAMSConfig config = ZAMSConfig.get();
        String version = Platform.getMod(ZAMegaShards.MOD_ID).getVersion();
        int total = countItems(ZAMSItemTags.MEGA_STONES);

        send(source, "title", ChatFormatting.GOLD, version);
        send(source, "total", ChatFormatting.WHITE, total);

        for (int i = 0; i < TIER_TAGS.size(); i++) {
            int tier = i + 1;
            send(source, "tier", ChatFormatting.GRAY, tier, countItems(TIER_TAGS.get(i)),
                    config.getBuyPrice(tier), config.getShardReturn(tier));
        }

        send(source, "configuration", ChatFormatting.GOLD);
        send(source, "disassembly", ChatFormatting.GRAY, status(config.isDisassemblyEnabled()));
        send(source, "deposits", ChatFormatting.GRAY, status(config.isMegaEnergyDepositsEnabled()));
        send(source, "max_uses", ChatFormatting.GRAY, config.getMaxTradesPerOffer());

        return 1;
    }

    private static int countItems(TagKey<Item> tag) {
        int count = 0;
        for (Holder<Item> ignored : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) count++;
        return count;
    }

    private static Component status(boolean enabled) {
        return Component.translatable("command.za_mega_shards.info." + (enabled ? "enabled" : "disabled"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private static void send(CommandSourceStack source, String key, ChatFormatting color, Object... args) {
        source.sendSuccess(() -> Component.translatable("command.za_mega_shards.info." + key, args).withStyle(color), false);
    }
}