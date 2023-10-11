package com.github.doiche.command;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class LogCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }

        switch (args.length) {
            case 1 -> {
                if(args[0].equals("item")) {
                    player.sendMessage(player.getInventory().getItemInMainHand().toString());
                    return true;
                }
                if(args[0].equals("test")) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    item.editMeta(itemMeta -> {
                        itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                                new AttributeModifier(UUID.randomUUID(), "test", 10, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
                    });
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
