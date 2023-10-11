package com.github.doiche.object.status;

import com.github.doiche.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public enum StatusType {
    ATTACK_POWER("기본 공격력"),
    CRITICAL("치명타 확률"),
    CRITICAL_DAMAGE("치명타 피해량"),
    HEALTH_POINT("추가 체력", EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET),
    DEFENSIVE_POWER("피해 감소율", EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET),
    BLOCK("회피율", EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET),
    AGILITY("이동 속도", EquipmentSlot.FEET),
    ;

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private final String ability;
    private final EquipmentSlot[] applicableSlots;

    StatusType(String ability, EquipmentSlot... applicableSlots) {
        this.ability = ability;
        this.applicableSlots = applicableSlots;
    }

    public static List<StatusType> getApplicableTypes(EquipmentSlot equipmentSlot) {
        List<StatusType> list = new ArrayList<>();
        for(StatusType type : StatusType.values()) {
            if(type.isApplicable(equipmentSlot)) {
                list.add(type);
            }
        }
        return list;
    }

    public boolean isPercent() {
        return this != ATTACK_POWER && this != HEALTH_POINT;
    }

    public String getAbility() {
        return ability;
    }

    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(Main.getInstance(), name().toLowerCase());
    }

    public Component lore(Rank rank, double value) {
        String unit = isPercent() ? "%" : "";
        return Component.text(" " + ability)
                .color(NamedTextColor.WHITE)
                .append(Component.text(" +" + format.format(value) + unit)
                        .color(rank.getColor()))
                .decoration(TextDecoration.ITALIC, false);
    }

    public EquipmentSlot[] getApplicableSlots() {
        return applicableSlots;
    }

    public boolean isApplicable(EquipmentSlot slot) {
        if(applicableSlots.length == 0) {
            return true;
        }
        for(EquipmentSlot applicableSlot : applicableSlots) {
            if(applicableSlot == slot) {
                return true;
            }
        }
        return false;
    }

    public Attribute getAttribute() {
        switch(this) {
            case HEALTH_POINT -> {
                return Attribute.GENERIC_MAX_HEALTH;
            }
            case ATTACK_POWER -> {
                return Attribute.GENERIC_ATTACK_DAMAGE;
            }
            case AGILITY -> {
                return Attribute.GENERIC_MOVEMENT_SPEED;
            }
            default -> {
                return null;
            }
        }
    }
}