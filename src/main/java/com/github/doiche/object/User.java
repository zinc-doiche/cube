package com.github.doiche.object;

import com.github.doiche.object.cube.OptionSlot;
import com.github.doiche.object.status.Status;
import com.github.doiche.object.status.StatusType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class User {
    private static final Map<String, User> users = new HashMap<>();

    private final String uuid;
    private final Map<StatusType, Status> statusMap = new EnumMap<>(StatusType.class);

    public static User getUser(String uuid) {
        return users.get(uuid);
    }
    public static void addUser(User user) {
        users.put(user.getUuid(), user);
    }
    public static void removeUser(String uuid) {
        users.remove(uuid);
    }

    public String getUuid() {
        return uuid;
    }

    public List<Status> getAllStatus() {
        return List.copyOf(statusMap.values());
    }

    public boolean hasStatus(StatusType statusType) {
        return statusMap.containsKey(statusType);
    }
    public Status getStatus(StatusType statusType) {
        return statusMap.get(statusType);
    }
    public Status addStatus(StatusType statusType, double value) {
        Status status = statusMap.get(statusType);
        status.addValue(value);
        return status;
    }
    public void setStatus(Status status) {
        statusMap.put(status.getType(), status);
    }
    public Status removeStatus(StatusType type) {
        return statusMap.remove(type);
    }

    public User(String uuid) {
        this.uuid = uuid;
    }

    public void onDisarm(Player player, PersistentDataContainer container) {
        for(OptionSlot slot : OptionSlot.values()) {
            if(container.has(slot.getNamespacedKey())) {
                String data = container.get(slot.getNamespacedKey(), PersistentDataType.STRING);
                if(data == null) {
                    continue;
                }
                String[] splitData = data.split(",");
                if(splitData.length != 2) {
                    continue;
                }
                StatusType statusType = StatusType.valueOf(splitData[0].toUpperCase());
                double value = Double.parseDouble(splitData[1]);
                Status status = addStatus(statusType, -value);
                if(getStatus(statusType).getValue() <= 0) {
                    removeStatus(statusType);
                    status.inactive(player);
                    continue;
                }
                status.active(player);
            }
        }
    }

    public void onEquip(Player player, PersistentDataContainer container) {
        for(OptionSlot slot : OptionSlot.values()) {
            if (container.has(slot.getNamespacedKey())) {
                String data = container.get(slot.getNamespacedKey(), PersistentDataType.STRING);
                if(data == null) {
                    continue;
                }
                String[] splitData = data.split(",");
                if(splitData.length != 2) {
                    continue;
                }
                StatusType statusType = StatusType.valueOf(splitData[0].toUpperCase());
                double value = Double.parseDouble(splitData[1]);
                Status status;
                if (!hasStatus(statusType)) {
                    status = new Status(statusType, value);
                    setStatus(status);
                    status.active(player);
                    continue;
                }
                status = addStatus(statusType, value);
                status.active(player);
            }
        }
    }
}
