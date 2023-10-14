package com.github.doiche.object.status;

import com.github.doiche.Main;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    public boolean isCriticalOrCriticalDamage() {
        return type == StatusType.CRITICAL || type == StatusType.CRITICAL_DAMAGE;
    }

    public Component lore() {
        Rank rank = StatusRegistry.getRank(type, value);
        if(rank == null) {
            return null;
        }
        return type.lore(rank, value);
    }

    public String serialize() {
        return type.name().toLowerCase() + "," + value;
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
        return type.isPercent()
                ? new AttributeModifier(UUID.randomUUID(), type.name(), value / 100, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
                : new AttributeModifier(UUID.randomUUID(), type.name(), value, AttributeModifier.Operation.ADD_NUMBER);
    }

    public AttributeModifier getModifier(EquipmentSlot equipmentSlot) {
        return new AttributeModifier(UUID.randomUUID(), type.name(), value, AttributeModifier.Operation.ADD_NUMBER, equipmentSlot);
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
        attribute.getModifiers().forEach(attributeModifier -> {
            if(attributeModifier.getName().equals(type.name()) && attributeModifier.getAmount() == value) {
                attribute.removeModifier(attributeModifier);
            }
        });
    }

    public void active(ItemStack item) {
        item.editMeta(meta -> active(item, meta));
    }

    public void active(ItemStack item, ItemMeta itemMeta) {
        Attribute attribute = type.getAttribute();
        if(attribute == null) {
            return;
        }
        ArrayListMultimap<Attribute, AttributeModifier> originalModifiers = ArrayListMultimap.create(item.getType().getDefaultAttributeModifiers(EquipmentSlot.HAND));
        Multimap<Attribute, AttributeModifier> modifiers = itemMeta.getAttributeModifiers();
        if(modifiers != null) {
            originalModifiers.putAll(modifiers);
        }
        originalModifiers.put(attribute, getModifier(EquipmentSlot.HAND));
        itemMeta.setAttributeModifiers(originalModifiers);
    }

    public void inactive(ItemMeta itemMeta) {
        Attribute attribute = type.getAttribute();
        if(attribute == null) {
            return;
        }
        Multimap<Attribute, AttributeModifier> attributeModifiers = itemMeta.getAttributeModifiers();
        if(attributeModifiers == null) {
            return;
        }
        attributeModifiers.forEach((attributeKey, attributeModifier) -> {
            if(attributeKey == attribute) {
                if(attributeModifier.getName().equals(type.name()) && attributeModifier.getAmount() == value) {
                    itemMeta.removeAttributeModifier(attribute, attributeModifier);
                }
            }
        });
    }

    @Override
    public String toString() {
        return "Status{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}
