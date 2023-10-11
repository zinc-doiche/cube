package com.github.doiche.command;

import com.github.doiche.object.cube.CubeRegistry;
import com.github.doiche.object.cube.OptionSlot;
import com.github.doiche.object.status.Rank;
import com.github.doiche.object.status.Status;
import com.github.doiche.object.status.StatusRegistry;
import com.github.doiche.object.status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.kyori.adventure.text.Component.empty;
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
                    return StringUtil.copyPartialMatches(args[1], Arrays.stream(OptionSlot.values()).map(OptionSlot::name).toList(), list);
                }
            }
            case 3 -> {
                if(args[0].equals("set")) {
                    return StringUtil.copyPartialMatches(args[2], Arrays.stream(StatusType.values()).map(StatusType::name).toList(), list);
                }
            }
            case 4 -> {
                if(args[0].equals("set")) {
                    return StringUtil.copyPartialMatches(args[3], Arrays.stream(Rank.values()).map(Rank::name).toList(), list);
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
                    item.editMeta(itemMeta -> {
                        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                        Status[] status = CubeRegistry.roll(item, container);
                        itemMeta.lore(Arrays.asList(
                                empty(),
                                status[0].lore(),
                                status[1].lore(),
                                status[2].lore()
                        ));
                        if(item.getType().getEquipmentSlot() == EquipmentSlot.HAND) {
                            for (Status statusInstance : status) {
                                if(statusInstance.getType() == StatusType.CRITICAL) {
                                    container.set(StatusType.CRITICAL.getNamespacedKey(), PersistentDataType.DOUBLE, statusInstance.getValue());
                                }
                                if(statusInstance.getType() == StatusType.CRITICAL_DAMAGE) {
                                    container.set(StatusType.CRITICAL_DAMAGE.getNamespacedKey(), PersistentDataType.DOUBLE, statusInstance.getValue());
                                }
                                if(statusInstance.getType() == StatusType.ATTACK_POWER) {
                                    statusInstance.active(itemMeta);
                                }
                            }
                        }
                        //player.sendMessage(Arrays.toString(status));
                    });
                    return true;
                }
            }
            case 4 -> {
                if(args[0].equals("set")) {
                    item.editMeta(itemMeta -> {
                        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                        OptionSlot optionSlot = OptionSlot.valueOf(args[1]);
                        StatusType statusType = StatusType.valueOf(args[2]);
                        double value = StatusRegistry.getRegistry(statusType).getValue(Rank.valueOf(args[3]));
                        StatusType originStatusType = StatusType.valueOf(container.get(optionSlot.getNamespacedKey(), PersistentDataType.STRING)
                                .split(",")[0].toUpperCase());
                        Status originalStatus = new Status(originStatusType, .0);
                        Status status = new Status(statusType, value);
                        List<Component> lore = itemMeta.lore();
                        lore.set(optionSlot.ordinal() + 1, status.lore());
                        itemMeta.lore(lore);
                        container.set(optionSlot.getNamespacedKey(), PersistentDataType.STRING, statusType.name().toLowerCase() + "," + value);

                        if(originStatusType.equals(StatusType.CRITICAL)) {
                            container.remove(StatusType.CRITICAL.getNamespacedKey());
                        }
                        if(originStatusType.equals(StatusType.CRITICAL_DAMAGE)) {
                            container.remove(StatusType.CRITICAL_DAMAGE.getNamespacedKey());
                        }
                        if(statusType.equals(StatusType.CRITICAL)) {
                            container.set(StatusType.CRITICAL.getNamespacedKey(), PersistentDataType.DOUBLE, value);
                        }
                        if(statusType.equals(StatusType.CRITICAL_DAMAGE)) {
                            container.set(StatusType.CRITICAL.getNamespacedKey(), PersistentDataType.DOUBLE, value);
                        }
                        if(originStatusType.equals(StatusType.ATTACK_POWER)) {
                            originalStatus.inactive(itemMeta);
                        }
                        if(statusType.equals(StatusType.ATTACK_POWER)) {
                            status.active(itemMeta);
                        }
                    });
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
