package com.github.doiche.command;

import com.github.doiche.object.status.Status;
import com.github.doiche.object.status.StatusType;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TestCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        switch (args.length) {
            case 1 -> {
                switch (args[0]) {
                    case "item" -> {
                        player.sendMessage(item.toString());
                        return true;
                    }
                    case "active" -> {
                        new Status(StatusType.ATTACK_POWER, 10).active(item);
                        return true;
                    }
                    case "inactive" -> {
                        item.editMeta(new Status(StatusType.ATTACK_POWER, 10)::inactive);
                        return true;
                    }
                    case "test" -> {
                        item.getType().getDefaultAttributeModifiers(EquipmentSlot.HAND).forEach((attribute, attributeModifiers) -> {
                            player.sendMessage(attribute.toString());
                            player.sendMessage(attributeModifiers.toString());
                        });
                        player.sendMessage("======");
                        Multimap<Attribute, AttributeModifier> modifiers = item.getItemMeta().getAttributeModifiers();
                        if(modifiers == null) {
                            player.sendMessage("no attribute");
                            return true;
                        }
                        modifiers.forEach((attribute, attributeModifiers) -> {
                            player.sendMessage(attribute.toString());
                            player.sendMessage(attributeModifiers.toString());
                        });
                    }
                }
            }
            case 2 -> {
                switch (args[0]) {
                    case "attribute" -> {
                        if(!item.hasItemMeta()) {
                            player.sendMessage("no meta");
                            return true;
                        }
                        Attribute attribute = Attribute.valueOf(args[1]);
                        Collection<AttributeModifier> attributeModifiers = item.getItemMeta().getAttributeModifiers(attribute);
                        if(attributeModifiers == null) {
                            player.sendMessage("no attribute");
                            return true;
                        }
                        player.sendMessage(attributeModifiers.toString());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                return StringUtil.copyPartialMatches(args[0], Arrays.asList("item", "active", "inactive", "attribute"), list);
            }
            case 2 -> {
                return StringUtil.copyPartialMatches(args[1], Arrays.stream(Attribute.values()).map(Attribute::name).toList(), list);
            }
        }
        return list;
    }
}
