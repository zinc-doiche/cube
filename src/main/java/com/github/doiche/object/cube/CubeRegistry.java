package com.github.doiche.object.cube;

import com.github.doiche.Main;
import com.github.doiche.object.status.Rank;
import com.github.doiche.object.status.Status;
import com.github.doiche.object.status.StatusRegistry;
import com.github.doiche.object.status.StatusType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public class CubeRegistry {
    private static final String FILE_NAME = "cube.yml";
    private static final Random random = new Random();
    private static final Map<OptionSlot, CubeRegistry> optionProbabilities = new EnumMap<>(OptionSlot.class);

    private final OptionSlot optionSlot;
    private final Map<Rank, Double> rankProbabilities = new EnumMap<>(Rank.class);
    private double sum;

    public static CubeRegistry getRegistry(OptionSlot optionSlot) {
        return optionProbabilities.get(optionSlot);
    }

    public OptionSlot getOptionSlot() {
        return optionSlot;
    }

    public double getProbability(Rank rank) {
        return rankProbabilities.get(rank);
    }

    public CubeRegistry(OptionSlot optionSlot) {
        this.optionSlot = optionSlot;
    }

    public Rank getRandomRank() {
        double cursor = .0;
        double x = random.nextDouble(sum);
        for(var entry : rankProbabilities.entrySet()) {
            cursor += entry.getValue();
            if(cursor > x) {
                return entry.getKey();
            }
        }
        return Rank.COMMON;
    }

    public static Status[] roll(EquipmentSlot equipmentSlot, PersistentDataContainer container) {
        Status[] status = new Status[3];
        for(OptionSlot slot : OptionSlot.values()) {
            var types = Arrays.stream(StatusType.values())
                    .filter(type -> type.isApplicable(equipmentSlot))
                    .toList();
            int index = random.nextInt(types.size());
            StatusType statusType = types.get(index);
            Rank rank = getRegistry(slot).getRandomRank();
            double value = StatusRegistry.getRegistry(statusType).getValue(rank);
            String data = statusType.name().toLowerCase() + "," + value;
            container.set(slot.getNamespacedKey(), PersistentDataType.STRING, data);
            status[slot.ordinal()] = new Status(statusType, value);
        }
        return status;
    }

    public static void read() {
        try {
            File file = new File(Main.getInstance().getDataFolder(), FILE_NAME);
            if(!file.exists()) {
                Main.getInstance().getSLF4JLogger().info("cube.yml 파일 생성 중...");
                Main.getInstance().saveResource(FILE_NAME, false);
            }
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            for (OptionSlot slot : OptionSlot.values()) {
                ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection(slot.name());
                if(configurationSection == null) {
                    continue;
                }
                CubeRegistry cubeRegistry = new CubeRegistry(slot);
                for (Rank rank : Rank.values()) {
                    double value = configurationSection.getDouble(rank.name());
                    cubeRegistry.rankProbabilities.put(rank, value);
                }
                cubeRegistry.sum = cubeRegistry.rankProbabilities.values().stream()
                        .reduce(Double::sum)
                        .orElse(100.0);
                optionProbabilities.put(slot, cubeRegistry);
            }
        } catch (Exception e) {
            Main.getInstance().getSLF4JLogger().error("cube.yml 파일을 읽는 도중 오류가 발생했습니다.", e);
        }

    }
}
