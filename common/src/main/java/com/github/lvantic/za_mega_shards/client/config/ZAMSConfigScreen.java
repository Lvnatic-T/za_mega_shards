package com.github.lvantic.za_mega_shards.client.config;

import com.github.lvantic.za_mega_shards.config.ZAMSConfig;
import com.github.lvantic.za_mega_shards.config.ZAMSConfigEditor;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ZAMSConfigScreen {

    private static final int[] DEFAULT_BUY = {64, 80, 96, 112, 128};
    private static final int[] DEFAULT_RETURN = {32, 40, 48, 56, 64};

    private ZAMSConfigScreen() {
    }

    public static Screen create(Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean editable = minecraft.level == null || minecraft.getSingleplayerServer() != null;
        ZAMSConfig config = ZAMSConfig.get();

        int[] buy = new int[5];
        int[] returns = new int[5];

        for (int i = 0; i < 5; i++) {
            buy[i] = config.getBuyPrice(i + 1);
            returns[i] = config.getShardReturn(i + 1);
        }

        boolean[] disassembly = {config.isDisassemblyEnabled()};
        boolean[] deposits = {config.isMegaEnergyDepositsEnabled()};
        int[] maxUses = {config.getMaxTradesPerOffer()};

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(text("title"))
                .setAlwaysShowTabs(true);

        if (editable) {
            builder.setSavingRunnable(() -> ZAMSConfigEditor.save(
                    buy, returns, disassembly[0], deposits[0], maxUses[0]
            ));
        }

        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(text("general"));
        ConfigCategory prices = builder.getOrCreateCategory(text("prices"));
        ConfigCategory returnsCategory = builder.getOrCreateCategory(text("returns"));

        if (!editable) {
            general.addEntry(entries.startTextDescription(text("remote")).build());
            prices.addEntry(entries.startTextDescription(text("remote")).build());
            returnsCategory.addEntry(entries.startTextDescription(text("remote")).build());
            return builder.build();
        }

        general.addEntry(entries.startTextDescription(legend()).build());
        general.addEntry(entries.startTextDescription(text("restart")).build());

        general.addEntry(entries.startBooleanToggle(setting("disassembly"), disassembly[0])
                .setDefaultValue(true)
                .setTooltip(text("tooltip.disassembly"))
                .setSaveConsumer(value -> disassembly[0] = value)
                .build());

        general.addEntry(entries.startBooleanToggle(setting("deposits"), deposits[0])
                .setDefaultValue(true)
                .setTooltip(text("tooltip.deposits"))
                .setSaveConsumer(value -> deposits[0] = value)
                .build());

        general.addEntry(entries.startIntSlider(setting("max_uses"), maxUses[0], 1, 64)
                .setDefaultValue(4)
                .setTooltip(text("tooltip.max_uses"))
                .setSaveConsumer(value -> maxUses[0] = value)
                .build());

        prices.addEntry(entries.startTextDescription(legend()).build());
        prices.addEntry(entries.startTextDescription(text("prices.description")).build());

        returnsCategory.addEntry(entries.startTextDescription(legend()).build());
        returnsCategory.addEntry(entries.startTextDescription(text("returns.description")).build());

        for (int i = 0; i < 5; i++) {
            final int index = i;

            prices.addEntry(entries.startIntField(tier(i + 1), buy[i])
                    .setDefaultValue(DEFAULT_BUY[i])
                    .setMin(1)
                    .setMax(128)
                    .setTooltip(text("tooltip.price"))
                    .setSaveConsumer(value -> buy[index] = value)
                    .build());

            returnsCategory.addEntry(entries.startIntField(tier(i + 1), returns[i])
                    .setDefaultValue(DEFAULT_RETURN[i])
                    .setMin(1)
                    .setMax(64)
                    .setTooltip(text("tooltip.return"))
                    .setSaveConsumer(value -> returns[index] = value)
                    .build());
        }

        return builder.build();
    }

    private static Component text(String key) {
        return Component.translatable("config.za_mega_shards." + key);
    }

    private static Component setting(String key) {
        return Component.empty()
                .append(Component.literal("■ ").withStyle(ChatFormatting.RED))
                .append(text(key));
    }

    private static Component tier(int tier) {
        return Component.empty()
                .append(Component.literal("■ ").withStyle(ChatFormatting.RED))
                .append(Component.translatable("config.za_mega_shards.tier", tier));
    }

    private static Component legend() {
        return Component.empty()
                .append(Component.literal("■ ").withStyle(ChatFormatting.RED))
                .append(text("legend"));
    }
}