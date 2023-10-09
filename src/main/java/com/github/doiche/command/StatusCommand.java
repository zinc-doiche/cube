package com.github.doiche.command;

import com.github.doiche.Main;
import com.github.doiche.object.User;
import com.github.doiche.object.status.Rank;
import com.github.doiche.object.status.Status;
import com.github.doiche.object.status.StatusRegistry;
import com.github.doiche.object.status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class StatusCommand implements TabExecutor {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch(args.length) {
            case 1 -> {
                return StringUtil.copyPartialMatches(args[0], Arrays.asList("add", "set", "remove", "info"), list);
            }
            case 2 -> {
                if(args[0].equals("add") || args[0].equals("set") || args[0].equals("remove")) {
                    return StringUtil.copyPartialMatches(args[1], Arrays.stream(StatusType.values()).map(StatusType::name).toList(), list);
                }
                return list;
            }
            case 3 -> {
                if(args[0].equals("add")) {
                    return StringUtil.copyPartialMatches(args[2], Arrays.stream(Rank.values()).map(Rank::name).toList(), list);
                }
                return list;
            }
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
            switch (args.length) {
                case 1 -> {
                    if (args[0].equals("info")) {
                        Component message = text("Enchants:").appendNewline();
                        for (Status status : user.getAllStatus()) {
                            Attribute attribute = status.getType().getAttribute();
                            double value;
                            if(attribute == null) {
                                value = status.getValue();
                            } else {
                                AttributeInstance attributeInstance = player.getAttribute(attribute);
                                value = attributeInstance == null ? status.getValue() : attributeInstance.getValue();
                            }
                            message = message
                                    .append(text(status.getType().name() + ": " + status.getValue() + " (" + value + ")"))
                                    .appendNewline();
                        }
                        player.sendMessage(message);
                        return true;
                    }
                }
                case 2 -> {
                    if(args[0].equals("remove")) {
                        try {
                            StatusType type = StatusType.valueOf(args[1]);
                            Status status = user.removeStatus(type);
                            status.inactive(player);
                            return true;
                        } catch (IllegalArgumentException e) {
                            Main.getInstance().getSLF4JLogger().error("Error while parsing command", e);
                        }
                    }
                }
                case 3 -> {
                    if(args[0].equals("add")) {
                        try {
                            StatusType type = StatusType.valueOf(args[1]);
                            Rank rank = Rank.valueOf(args[2]);
                            Double value = StatusRegistry.getRegistry(type).getValue(rank);
                            Status status = new Status(type, value);
                            user.setStatus(status);
                            status.active(player);
                            return true;
                        } catch (IllegalArgumentException e) {
                            Main.getInstance().getSLF4JLogger().error("Error while parsing command", e);
                        }
                    } else if(args[0].equals("set")) {
                        try {
                            StatusType type = StatusType.valueOf(args[1]);
                            double value = Double.parseDouble(args[2]);
                            Status status = new Status(type, value);
                            user.setStatus(status);
                            status.active(player);
                            return true;
                        } catch (NumberFormatException e) {
                            Main.getInstance().getSLF4JLogger().error("Error while parsing command", e);
                        }
                    }
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
