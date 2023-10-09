package com.github.doiche.command;

import com.github.doiche.object.cube.CubeRegistry;
import com.github.doiche.object.status.StatusType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class CubeCommand implements TabExecutor {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                return StringUtil.copyPartialMatches(args[0], Arrays.asList("roll", "set", "info"), list);
            }
            case 2 -> {
                if(args[0].equals("set")) {
                    return StringUtil.copyPartialMatches(args[1], Arrays.asList("1", "2", "3"), list);
                }
            }
            case 3 -> {
                if(args[0].equals("set")) {
                    return StringUtil.copyPartialMatches(args[2], Arrays.stream(StatusType.values()).map(StatusType::name).toList(), list);
                }
            }
            case 4 -> {
                if(args[0].equals("set")) {
                    return StringUtil.copyPartialMatches(args[3], Collections.singletonList("value"), list);
                }
            }
        }
        return list;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }
        ItemStack item = player.getInventory().getItemInMainHand();

        if(item.isEmpty()) {
            player.sendMessage(text("아이템을 들고 명령어를 사용해주세요.")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true));
            return false;
        }

        switch(args.length) {
            case 1 -> {
                if(args[0].equals("info")) {
                    player.sendMessage(item.displayName()
                            .append(text(" 정보:")).appendNewline()
                            .append(text("")).appendNewline()
                            .append(text("")));
                    return true;
                }
                if(args[0].equals("roll")) {
                    PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
                    CubeRegistry.roll(item.getType().getEquipmentSlot(), container);
                    return true;
                }
            }
            case 4 -> {
                if(args[0].equals("set")) {
                    player.sendMessage(text("Cube Set").color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true));
                    return true;
                }
            }
        }

        player.sendMessage(text("/cube info").appendNewline()
                .append(text("/cube roll")).appendNewline()
                .append(text("/cube set <order> <StatusType> <value>"))
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true));

        return false;
    }
}
