package com.github.doiche.object.status;

import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;

public enum StatusType {
    ATTACK_POWER("STR"),
    CRITICAL("CT"),
    CRITICAL_DAMAGE("CTD"),
    HEALTH_POINT("HP", EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET),
    DEFENSIVE_POWER("DFE", EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET),
    BLOCK("BK", EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET),
    AGILITY("AGI", EquipmentSlot.FEET),
    ;

    private final String brief;
    private final EquipmentSlot[] applicableSlots;

    StatusType(String brief, EquipmentSlot... applicableSlots) {
        this.brief = brief;
        this.applicableSlots = applicableSlots;
    }

    public String getBrief() {
        return brief;
    }

    public EquipmentSlot[] getApplicableSlots() {
        return applicableSlots;
    }

    public boolean isApplicable(EquipmentSlot slot) {
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
