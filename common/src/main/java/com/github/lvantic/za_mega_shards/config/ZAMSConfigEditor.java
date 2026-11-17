package com.github.lvantic.za_mega_shards.config;

import com.github.lvantic.za_mega_shards.ZAMegaShards;
import dev.architectury.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ZAMSConfigEditor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZAMegaShards.MOD_ID);

    private ZAMSConfigEditor() {
    }

    public static boolean save(int[] buyPrices, int[] shardReturns, boolean disassembly, boolean deposits, int maxUses) {
        if (buyPrices.length != 5 || shardReturns.length != 5) {
            throw new IllegalArgumentException("Expected five Mega Stone tiers.");
        }

        Map<String, LinkedHashMap<String, String>> sections = new LinkedHashMap<>();
        LinkedHashMap<String, String> prices = new LinkedHashMap<>();
        LinkedHashMap<String, String> returns = new LinkedHashMap<>();

        for (int i = 0; i < 5; i++) {
            prices.put("tier_" + (i + 1), String.valueOf(Math.clamp(buyPrices[i], 1, 128)));
            returns.put("tier_" + (i + 1), String.valueOf(Math.clamp(shardReturns[i], 1, 64)));
        }

        sections.put("[mega_stone_buy_prices]", prices);
        sections.put("[mega_stone_shard_returns]", returns);
        sections.put("[research_station]", setting("enable_disassembly", disassembly));
        sections.put("[world_generation]", setting("enable_mega_energy_deposits", deposits));
        sections.put("[mega_researcher]", setting("max_trades_per_offer", Math.clamp(maxUses, 1, 64)));

        Path path = Platform.getConfigFolder().resolve("za_mega_shards.toml");

        try {
            if (Files.notExists(path)) ZAMSConfig.load();

            String original = Files.readString(path, StandardCharsets.UTF_8);
            String newline = original.contains("\r\n") ? "\r\n" : "\n";
            List<String> output = new ArrayList<>();
            Map<String, Set<String>> seen = new HashMap<>();
            Set<String> seenSections = new HashSet<>();
            String currentSection = null;

            for (String line : original.split("\\R", -1)) {
                String trimmed = line.trim();

                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    appendMissing(output, currentSection, sections, seen);
                    currentSection = trimmed;
                    seenSections.add(trimmed);
                    output.add(line);
                    continue;
                }

                LinkedHashMap<String, String> entries = sections.get(currentSection);

                if (entries != null) {
                    int equals = line.indexOf('=');

                    if (equals >= 0) {
                        String key = line.substring(0, equals).trim();

                        if (entries.containsKey(key)) {
                            if (seen.computeIfAbsent(currentSection, ignored -> new HashSet<>()).add(key)) {
                                int comment = line.indexOf('#', equals + 1);
                                String suffix = comment >= 0 ? " " + line.substring(comment).trim() : "";
                                output.add(line.substring(0, equals + 1) + " " + entries.get(key) + suffix);
                            }
                            continue;
                        }
                    }
                }

                output.add(line);
            }

            appendMissing(output, currentSection, sections, seen);

            for (var section : sections.entrySet()) {
                if (seenSections.contains(section.getKey())) continue;

                output.add("");
                output.add(section.getKey());

                for (var entry : section.getValue().entrySet()) {
                    output.add(entry.getKey() + " = " + entry.getValue());
                }
            }

            Files.writeString(path, String.join(newline, output), StandardCharsets.UTF_8);
            ZAMSConfig.load();
            return true;
        } catch (IOException exception) {
            LOGGER.error("Failed to save ZA Mega Shards configuration.", exception);
            return false;
        }
    }

    private static LinkedHashMap<String, String> setting(String key, Object value) {
        LinkedHashMap<String, String> entries = new LinkedHashMap<>();
        entries.put(key, String.valueOf(value));
        return entries;
    }

    private static void appendMissing(
            List<String> output,
            String section,
            Map<String, LinkedHashMap<String, String>> sections,
            Map<String, Set<String>> seen
    ) {
        LinkedHashMap<String, String> entries = sections.get(section);
        if (entries == null) return;

        Set<String> found = seen.getOrDefault(section, Set.of());

        for (var entry : entries.entrySet()) {
            if (!found.contains(entry.getKey())) {
                output.add(entry.getKey() + " = " + entry.getValue());
            }
        }
    }
}