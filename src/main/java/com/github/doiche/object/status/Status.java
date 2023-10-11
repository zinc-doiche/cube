package com.github.doiche.object.status;

import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class Status {
    private final StatusType type;
    private double value;

    public StatusType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public void addValue(double value) {
        this.value += value;
    }

    public Status(StatusType type, double value) {
        this.type = type;
        this.value = value;
    }

    public Component lore() {
        Rank rank = StatusRegistry.getRank(type, value);
        if(rank == null) {
            return null;
        }
        return type.lore(rank, value);
    }

    private AttributeInstance getAttribute(Player player) {
        AttributeInstance attribute;
        switch(type) {
            case HEALTH_POINT -> {
                attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            }
            case ATTACK_POWER -> {
                attribute = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            }
            case AGILITY -> {
                attribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            }
            default -> {
                return null;
            }
        }
        return attribute;
    }

    public AttributeModifier getModifier() {
        UUID uuid = UUID.nameUUIDFromBytes(type.name().getBytes());

        return type.isPercent()
                ? new AttributeModifier(uuid, type.name(), value / 100, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
                : new AttributeModifier(uuid, type.name(), value, AttributeModifier.Operation.ADD_NUMBER);
    }

    public void active(Player player) {
        AttributeInstance attribute = getAttribute(player);
        if(attribute == null) {
            return;
        }
        AttributeModifier modifier = getModifier();
        attribute.removeModifier(modifier);
        attribute.addModifier(modifier);
    }

    public void inactive(Player player) {
        AttributeInstance attribute = getAttribute(player);
        if(attribute == null) {
            return;
        }
        attribute.removeModifier(getModifier());
    }

    public void active(ItemMeta itemMeta) {
        Attribute attribute = type.getAttribute();
        if(attribute == null) {
            return;
        }
        AttributeModifier modifier = new AttributeModifier(UUID.nameUUIDFromBytes(type.name().getBytes()), type.name(),
                value, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        itemMeta.removeAttributeModifier(attribute);
        itemMeta.addAttributeModifier(attribute, modifier);
    }

    public void inactive(ItemMeta itemMeta) {
        Attribute attribute = type.getAttribute();
        if(attribute == null) {
            return;
        }
        itemMeta.removeAttributeModifier(attribute);
    }

    @Override
    public String toString() {
        return "Status{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}
