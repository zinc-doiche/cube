package com.github.doiche.command;

import com.github.doiche.object.User;
import com.github.doiche.object.cube.OptionSlot;
import com.github.doiche.object.status.Status;
import com.github.doiche.object.status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class StatusCommand implements TabExecutor {
    private static final DecimalFormat format = new DecimalFormat("#.##");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("info", "clear"), list);
        }
        return list;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }
        User user = User.getUser(player.getUniqueId().toString());
        if(user != null) {
            if (args.length == 1) {
                if (args[0].equals("info")) {
                    Component message = text(player.getName() + ":").appendNewline();
                    ItemStack item = player.getInventory().getItemInMainHand();
                    Status[] arStatus = new Status[3];
                    if (!item.isEmpty()) {
                        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
                        if (container.has(OptionSlot.FIRST.getNamespacedKey())) {
                            for (OptionSlot optionSlot : OptionSlot.values()) {
                                String data = container.get(optionSlot.getNamespacedKey(), PersistentDataType.STRING);
                                String[] split = data.split(",");
                                StatusType statusType = StatusType.valueOf(split[0].toUpperCase());
                                double value = Double.parseDouble(split[1]);
                                arStatus[optionSlot.ordinal()] = new Status(statusType, value);
                            }
                        }
                    }
                    for (Status status : user.getAllStatus()) {
                        Attribute attribute = status.getType().getAttribute();
                        double statusValue = status.getValue();
                        double realValue;
                        if (attribute == null) {
                            realValue = status.getValue();
                        } else {
                            AttributeInstance attributeInstance = player.getAttribute(attribute);
                            realValue = attributeInstance == null ? status.getValue() : attributeInstance.getValue();
                        }
                        if (!item.isEmpty()) {
                            for (Status handStatus : arStatus) {
                                if (handStatus.getType() != status.getType()) {
                                    continue;
                                }
                                if (attribute == null) {
                                    realValue += handStatus.getValue();
                                }
                                statusValue += handStatus.getValue();
                            }
                        }
                        message = message
                                .append(text(status.getType().name() + ": " + format.format(statusValue) + " (" + format.format(realValue) + ")"))
                                .appendNewline();
                    }
                    player.sendMessage(message);
                    return true;
                } else if (args[0].equals("clear")) {
                    for (Status status : user.getAllStatus()) {
                        user.removeStatus(status.getType());
                    }
                    for (Attribute attribute : Attribute.values()) {
                        AttributeInstance attributeInstance = player.getAttribute(attribute);
                        if (attributeInstance == null) {
                            continue;
                        }
                        for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                            attributeInstance.removeModifier(modifier);
                        }
                    }
                    return true;
                }
            }
        }

        sender.sendMessage(text("usage:", NamedTextColor.RED, TextDecoration.BOLD)
                .appendNewline()
                .append(text("/enchant add <status> <rank>"))
                .appendNewline()
                .append(text("/enchant remove <status>"))
                .appendNewline()
                .append(text("/enchant info")));

        return false;
    }
}
