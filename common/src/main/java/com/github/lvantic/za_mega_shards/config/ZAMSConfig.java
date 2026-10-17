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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public final class ZAMSConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZAMegaShards.MOD_ID);
    private static final int[] DEFAULT_BUY = {64, 80, 96, 112, 128};
    private static final int[] DEFAULT_RETURN = {32, 40, 48, 56, 64};
    private static final boolean DEFAULT_DISASSEMBLY = true;
    private static final boolean DEFAULT_DEPOSITS = true;
    private static final int DEFAULT_MAX_USES = 4;

    private static final String BUY_COMMENTS = """
            # Prices when trading with the Mega Researcher Villager
            # DEFAULT: 64, 80, 96, 112, 128
            # RANGE: 1 - 128 anything outside will result in the DEFAULT being used
            """;

    private static final String RETURN_COMMENTS = """
            # Worth of Stones when using the Mega Research Station
            # DEFAULT: 32, 40, 48, 56, 64
            # RANGE: 1 - 64 anything outside will result in the DEFAULT being used
            """;

    private static final String DISASSEMBLY_COMMENTS = """
            # Whether Mega Stones can be disassembled into Mega Shards
            # The Research Station can still be used as a villager workstation when disabled
            # DEFAULT: true
            """;

    private static final String DEPOSIT_COMMENTS = """
            # Whether Mega Energy Deposits generate naturally
            # Existing deposits are not removed when disabled
            # Requires a game/server restart to take effect
            # DEFAULT: true
            """;

    private static final String TRADE_COMMENTS = """
            # Maximum number of uses per Mega Researcher trade before restocking
            # Existing villager offers retain their previously generated limits
            # DEFAULT: 4
            # RANGE: 1 - 64 anything outside will result in the DEFAULT being used
            """;

    private static volatile ZAMSConfig instance = defaults();

    private final int[] buyPrices;
    private final int[] shardReturns;
    private final boolean enableDisassembly;
    private final boolean enableMegaEnergyDeposits;
    private final int maxTradesPerOffer;

    private ZAMSConfig(int[] buyPrices, int[] shardReturns, boolean enableDisassembly, boolean enableMegaEnergyDeposits, int maxTradesPerOffer) {
        this.buyPrices = buyPrices.clone();
        this.shardReturns = shardReturns.clone();
        this.enableDisassembly = enableDisassembly;
        this.enableMegaEnergyDeposits = enableMegaEnergyDeposits;
        this.maxTradesPerOffer = maxTradesPerOffer;
    }

    private static ZAMSConfig defaults() {
        return new ZAMSConfig(DEFAULT_BUY, DEFAULT_RETURN, DEFAULT_DISASSEMBLY, DEFAULT_DEPOSITS, DEFAULT_MAX_USES);
    }

    public static ZAMSConfig get() {
        return instance;
    }

    public int getBuyPrice(int tier) {
        return tier >= 1 && tier <= 5 ? buyPrices[tier - 1] : 0;
    }

    public int getShardReturn(int tier) {
        return tier >= 1 && tier <= 5 ? shardReturns[tier - 1] : 0;
    }

    public boolean isDisassemblyEnabled() {
        return enableDisassembly;
    }

    public boolean isMegaEnergyDepositsEnabled() {
        return enableMegaEnergyDeposits;
    }

    public int getMaxTradesPerOffer() {
        return maxTradesPerOffer;
    }

    public static void load() {
        Path path = Platform.getConfigFolder().resolve("za_mega_shards.toml");
        ZAMSConfig defaultConfig = defaults();

        try {
            Files.createDirectories(path.getParent());

            if (Files.notExists(path)) {
                Files.writeString(path, formatToml(defaultConfig), StandardCharsets.UTF_8);
                instance = defaultConfig;
                LOGGER.info("Created default ZA Mega Shards config at {}", path);
                return;
            }

            String contents = Files.readString(path, StandardCharsets.UTF_8);
            String updated = mergeDefaults(contents);
            instance = readToml(updated);

            if (!updated.equals(contents)) {
                try {
                    Files.writeString(path, updated, StandardCharsets.UTF_8);
                    LOGGER.info("Updated ZA Mega Shards config.");
                } catch (IOException exception) {
                    LOGGER.warn("Could not update ZA Mega Shards config at {}", path, exception);
                }
            }

            LOGGER.info("Loaded ZA Mega Shards config from {}", path);
        } catch (IOException | RuntimeException exception) {
            instance = defaultConfig;
            LOGGER.error("Failed to load ZA Mega Shards config. Using default values.", exception);
        }
    }

    private static ZAMSConfig readToml(String contents) {
        int[] buy = DEFAULT_BUY.clone();
        int[] returns = DEFAULT_RETURN.clone();
        boolean enableDisassembly = DEFAULT_DISASSEMBLY;
        boolean enableDeposits = DEFAULT_DEPOSITS;
        int maxUses = DEFAULT_MAX_USES;
        boolean[][] foundTiers = new boolean[2][5];
        boolean[] foundOptions = new boolean[3];
        int section = -1;

        for (String rawLine : contents.split("\\R")) {
            String line = removeComment(rawLine);
            if (line.isEmpty()) continue;

            if (isSection(line)) {
                section = switch (line) {
                    case "[mega_stone_buy_prices]" -> 0;
                    case "[mega_stone_shard_returns]" -> 1;
                    case "[research_station]" -> 2;
                    case "[world_generation]" -> 3;
                    case "[mega_researcher]" -> 4;
                    default -> -1;
                };
                continue;
            }

            if (section == -1) continue;

            int equals = line.indexOf('=');
            if (equals < 0) continue;

            String key = line.substring(0, equals).trim();
            String value = line.substring(equals + 1).trim();

            if (section <= 1) {
                if (!key.matches("tier_[1-5]")) continue;

                int tier = key.charAt(5) - '1';
                String group = section == 0 ? "mega_stone_buy_prices" : "mega_stone_shard_returns";
                int[] values = section == 0 ? buy : returns;
                int[] defaults = section == 0 ? DEFAULT_BUY : DEFAULT_RETURN;
                int maximum = section == 0 ? 128 : 64;

                if (foundTiers[section][tier]) {
                    LOGGER.warn("Duplicate '{}.{}'; using default {}.", group, key, defaults[tier]);
                    values[tier] = defaults[tier];
                    continue;
                }

                foundTiers[section][tier] = true;
                values[tier] = readValue(value, group, key, defaults[tier], maximum);
                continue;
            }

            if (section == 2 && key.equals("enable_disassembly")) {
                if (foundOptions[0]) {
                    LOGGER.warn("Duplicate 'research_station.enable_disassembly'; using default {}.", DEFAULT_DISASSEMBLY);
                    enableDisassembly = DEFAULT_DISASSEMBLY;
                } else {
                    foundOptions[0] = true;
                    enableDisassembly = readBoolean(value, "research_station", key, DEFAULT_DISASSEMBLY);
                }
            } else if (section == 3 && key.equals("enable_mega_energy_deposits")) {
                if (foundOptions[1]) {
                    LOGGER.warn("Duplicate 'world_generation.enable_mega_energy_deposits'; using default {}.", DEFAULT_DEPOSITS);
                    enableDeposits = DEFAULT_DEPOSITS;
                } else {
                    foundOptions[1] = true;
                    enableDeposits = readBoolean(value, "world_generation", key, DEFAULT_DEPOSITS);
                }
            } else if (section == 4 && key.equals("max_trades_per_offer")) {
                if (foundOptions[2]) {
                    LOGGER.warn("Duplicate 'mega_researcher.max_trades_per_offer'; using default {}.", DEFAULT_MAX_USES);
                    maxUses = DEFAULT_MAX_USES;
                } else {
                    foundOptions[2] = true;
                    maxUses = readValue(value, "mega_researcher", key, DEFAULT_MAX_USES, 64);
                }
            }
        }

        return new ZAMSConfig(buy, returns, enableDisassembly, enableDeposits, maxUses);
    }

    private static int readValue(String value, String group, String key, int fallback, int maximum) {
        try {
            if (value.matches("[+-]?(0|[1-9](?:_?[0-9])*)")) {
                int number = Integer.parseInt(value.replace("_", ""));
                if (number >= 1 && number <= maximum) return number;
            }
        } catch (NumberFormatException ignored) {
        }

        LOGGER.warn("'{}.{}' must be an integer between 1 and {}; using default {}.", group, key, maximum, fallback);
        return fallback;
    }

    private static boolean readBoolean(String value, String group, String key, boolean fallback) {
        if (value.equals("true")) return true;
        if (value.equals("false")) return false;

        LOGGER.warn("'{}.{}' must be true or false; using default {}.", group, key, fallback);
        return fallback;
    }

    private static String formatToml(ZAMSConfig config) {
        StringBuilder result = new StringBuilder();
        appendSection(result, BUY_COMMENTS, "mega_stone_buy_prices", config.buyPrices);
        result.append("\n\n");
        appendSection(result, RETURN_COMMENTS, "mega_stone_shard_returns", config.shardReturns);
        appendSetting(result, DISASSEMBLY_COMMENTS, "research_station", "enable_disassembly", config.enableDisassembly);
        appendSetting(result, DEPOSIT_COMMENTS, "world_generation", "enable_mega_energy_deposits", config.enableMegaEnergyDeposits);
        appendSetting(result, TRADE_COMMENTS, "mega_researcher", "max_trades_per_offer", config.maxTradesPerOffer);
        return result.toString();
    }

    private static void appendSection(StringBuilder result, String comments, String name, int[] values) {
        result.append(comments).append('\n').append('[').append(name).append("]\n");
        for (int i = 0; i < values.length; i++) {
            result.append("tier_").append(i + 1).append(" = ").append(values[i]).append('\n');
        }
    }

    private static void appendSetting(StringBuilder result, String comments, String name, String key, Object value) {
        result.append("\n\n").append(comments).append('\n').append('[').append(name).append("]\n");
        result.append(key).append(" = ").append(value).append('\n');
    }

    private static String mergeDefaults(String contents) {
        String newline = contents.contains("\r\n") ? "\r\n" : "\n";
        List<String> lines = new ArrayList<>(Arrays.asList(contents.split("\\R", -1)));

        for (DefaultSection section : getDefaultSections()) {
            int headerIndex = findSection(lines, section.header());

            if (headerIndex == -1) {
                while (!lines.isEmpty() && lines.getLast().isEmpty()) lines.removeLast();
                if (!lines.isEmpty()) { lines.add(""); lines.add(""); }
                lines.addAll(section.comments());
                lines.add(section.header());
                lines.addAll(section.entries().values());
                lines.add("");
                continue;
            }

            restoreComments(lines, headerIndex, section.comments());
            headerIndex = findSection(lines, section.header());
            int end = headerIndex + 1;
            int insertAt = end;
            Set<String> present = new HashSet<>();

            while (end < lines.size() && !isSection(removeComment(lines.get(end)))) {
                String line = removeComment(lines.get(end));
                int equals = line.indexOf('=');

                if (equals >= 0) {
                    present.add(line.substring(0, equals).trim());
                    insertAt = end + 1;
                }

                end++;
            }

            for (var entry : section.entries().entrySet()) {
                if (!present.contains(entry.getKey())) {
                    lines.add(insertAt++, entry.getValue());
                }
            }
        }

        return String.join(newline, lines);
    }

    private static List<DefaultSection> getDefaultSections() {
        List<DefaultSection> sections = new ArrayList<>();
        List<String> pendingComments = new ArrayList<>();
        DefaultSection current = null;

        for (String line : formatToml(defaults()).split("\\R")) {
            String trimmed = line.trim();

            if (trimmed.startsWith("#")) {
                pendingComments.add(line);
            } else if (trimmed.isEmpty() && !pendingComments.isEmpty()) {
                pendingComments.add("");
            } else if (isSection(trimmed)) {
                current = new DefaultSection(trimmed, new ArrayList<>(pendingComments), new LinkedHashMap<>());
                sections.add(current);
                pendingComments.clear();
            } else if (current != null && trimmed.contains("=")) {
                String key = trimmed.substring(0, trimmed.indexOf('=')).trim();
                current.entries().put(key, line);
                pendingComments.clear();
            }
        }

        return sections;
    }

    private static void restoreComments(List<String> lines, int headerIndex, List<String> expected) {
        int start = headerIndex;
        while (start > 0) {
            String line = lines.get(start - 1).trim();
            if (!line.isEmpty() && !line.startsWith("#")) break;
            start--;
        }

        List<String> replacement = new ArrayList<>();
        if (start > 0) { replacement.add(""); replacement.add(""); }
        replacement.addAll(expected);
        if (lines.subList(start, headerIndex).equals(replacement)) return;
        lines.subList(start, headerIndex).clear();
        lines.addAll(start, replacement);
    }

    private static int findSection(List<String> lines, String header) {
        for (int i = 0; i < lines.size(); i++) {
            if (removeComment(lines.get(i)).equals(header)) return i;
        }
        return -1;
    }

    private static boolean isSection(String line) {
        return line.startsWith("[") && line.endsWith("]");
    }

    private static String removeComment(String line) {
        return line.split("#", 2)[0].trim();
    }

    private record DefaultSection(String header, List<String> comments, LinkedHashMap<String, String> entries) {
    }
}