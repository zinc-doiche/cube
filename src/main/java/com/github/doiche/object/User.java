package com.github.doiche.object;

import com.github.doiche.object.cube.OptionSlot;
import com.github.doiche.object.status.Status;
import com.github.doiche.object.status.StatusType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void addStatus(StatusType statusType, double value) {
        statusMap.get(statusType).addValue(value);
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

    public void onDisarm(PersistentDataContainer container) {
        for(OptionSlot slot : OptionSlot.values()) {
            if(container.has(slot.getNamespacedKey())) {
                String data = container.get(slot.getNamespacedKey(), PersistentDataType.STRING);
                if(data == null) {
                    return;
                }
                String[] splitData = data.split(",");
                if(splitData.length != 2) {
                    return;
                }
                StatusType statusType = StatusType.valueOf(splitData[0]);
                double value = Double.parseDouble(splitData[1]);
                addStatus(statusType, -value);
                if(getStatus(statusType).getValue() <= 0) {
                    removeStatus(statusType);
                }
            }
        }
    }

    public void onEquip(PersistentDataContainer container) {
        for(OptionSlot slot : OptionSlot.values()) {
            if (container.has(slot.getNamespacedKey())) {
                String data = container.get(slot.getNamespacedKey(), PersistentDataType.STRING);
                if(data == null) {
                    return;
                }
                String[] splitData = data.split(",");
                if(splitData.length != 2) {
                    return;
                }
                StatusType statusType = StatusType.valueOf(splitData[0]);
                double value = Double.parseDouble(splitData[1]);
                if (!hasStatus(statusType)) {
                    setStatus(new Status(statusType, value));
                    return;
                }
                addStatus(statusType, value);
            }
        }
    }
}
