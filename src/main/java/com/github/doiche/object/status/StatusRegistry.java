package com.github.doiche.object.status;

import com.github.doiche.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class StatusRegistry {
    private static final Map<StatusType, StatusRegistry> registries = new EnumMap<>(StatusType.class);
    private static final Random random = new Random();

    private final StatusType statusType;
    private final Map<Rank, Double[]> values;

    public static StatusRegistry getRegistry(StatusType statusType) {
        return registries.get(statusType);
    }

    public StatusRegistry(StatusType statusType, Map<Rank, Double[]> values) {
        this.statusType = statusType;
        this.values = values;
    }

    public Double getValue(Rank rank) {
        Double[] arValue = values.get(rank);
        if(arValue == null) {
            return -1.0;
        }
        switch(arValue.length) {
            case 1 -> {
                return arValue[0];
            }
            case 2 -> {
                return Math.abs(arValue[1] - arValue[0]) * random.nextDouble() + Math.min(arValue[0], arValue[1]);
            }
            default -> {
                return -1.0;
            }
        }
    }

    public static Rank getRank(StatusType statusType, double value) {
        StatusRegistry statusRegistry = registries.get(statusType);
        if(statusRegistry == null) {
            return null;
        }
        for(Rank rank : Rank.values()) {
            Double[] arValue = statusRegistry.values.get(rank);
            if(arValue == null) {
                continue;
            }
            switch(arValue.length) {
                case 1 -> {
                    if(arValue[0] == value) {
                        return rank;
                    }
                }
                case 2 -> {
                    if(arValue[0] <= value && value <= arValue[1]) {
                        return rank;
                    }
                }
            }
        }
        //fallback for custom debugging
        Main.getInstance().getSLF4JLogger().warn("fallback for custom debugging");
        return Rank.COMMON;
    }

    public static void read() {
        File file = new File(Main.getInstance().getDataFolder(), "status.yml");

        if(!file.exists()) {
            Main.getInstance().saveResource("status.yml", false);
        }

        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);

        for(StatusType statusType : StatusType.values()) {
            ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection(statusType.name());
            Map<Rank, Double[]> rankValues = new EnumMap<>(Rank.class);
            if(configurationSection == null) {
                continue;
            }
            for(Rank rank : Rank.values()) {
                String value = configurationSection.getString(rank.name());
                if(value == null) {
                    continue;
                }
                try {
                    Double[] arDouble;
                    if(value.contains(" ")) {
                        String[] arValue = value.split(" ");
                        if(arValue.length != 2) {
                            continue;
                        }
                        arDouble = new Double[2];
                        arDouble[0] = Double.parseDouble(arValue[0]);
                        arDouble[1] = Double.parseDouble(arValue[1]);
                    } else {
                        arDouble = new Double[1];
                        arDouble[0] = Double.parseDouble(value);
                    }
                    rankValues.put(rank, arDouble);
                } catch (NumberFormatException e) {
                    Main.getInstance().getSLF4JLogger().error("Error while parsing status.yml", e);
                }
            }
            registries.put(statusType, new StatusRegistry(statusType, rankValues));
        }
    }
}
