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
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
                    ItemMeta itemMeta = item.getItemMeta();
                    EquipmentSlot equipmentSlot = item.getType().getEquipmentSlot();
                    PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                    final List<Component> lore = new ArrayList<>();
                    lore.add(empty());
                    if(equipmentSlot.isArmor()) {
                        Arrays.stream(OptionSlot.values()).forEach(optionSlot -> {
                            NamespacedKey key = optionSlot.getNamespacedKey();
                            Status status = CubeRegistry.roll(optionSlot, equipmentSlot);
                            container.set(key, PersistentDataType.STRING, status.serialize());
                            lore.add(status.lore());
                        });
                    } else {
                        Arrays.stream(OptionSlot.values()).forEach(optionSlot -> {
                            Status status = CubeRegistry.roll(optionSlot, equipmentSlot);
                            lore.add(status.lore());
                            switch (status.getType()) {
                                case ATTACK_POWER -> {
                                    UUID uuid = UUID.nameUUIDFromBytes(status.getType().name().getBytes());
                                    AttributeModifier modifier = new AttributeModifier(uuid, status.getType().name(), status.getValue(),
                                            AttributeModifier.Operation.ADD_NUMBER, equipmentSlot);
                                    itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
                                }
                                case CRITICAL, CRITICAL_DAMAGE -> {
                                    NamespacedKey key = status.getType().getNamespacedKey();
                                    container.set(key, PersistentDataType.DOUBLE, status.getValue());
                                }
                            }
                        });
                    }
                    itemMeta.lore(lore);
                    item.setItemMeta(itemMeta);
                    return true;
                }
            }
            case 4 -> {
                if(args[0].equals("set")) {
                    ItemMeta itemMeta = item.getItemMeta();
                    PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                    OptionSlot optionSlot = OptionSlot.valueOf(args[1]);
                    StatusType statusType = StatusType.valueOf(args[2]);
                    double value = StatusRegistry.getRegistry(statusType).getValue(Rank.valueOf(args[3]));
                    String originStatusType = container.get(optionSlot.getNamespacedKey(), PersistentDataType.STRING).split(",")[0].toUpperCase();

                    String[] data = container.get(optionSlot.getNamespacedKey(), PersistentDataType.STRING).split(",");
                    Status oldStatus = new Status(StatusType.valueOf(data[0]), Double.parseDouble(data[1]));
                    Status newStatus = new Status(statusType, value);
                    List<Component> lore = itemMeta.lore();

                    if(item.getType().getEquipmentSlot().isArmor()) {

                    } else {

                    }

                    lore.set(optionSlot.ordinal() + 1, new Status(statusType, value).lore());
                    itemMeta.lore(lore);
                    container.set(optionSlot.getNamespacedKey(), PersistentDataType.STRING, statusType.name().toLowerCase() + "," + value);

                    if(originStatusType.equals(StatusType.CRITICAL.name())) {
                        container.remove(StatusType.CRITICAL.getNamespacedKey());
                    }
                    if(originStatusType.equals(StatusType.CRITICAL_DAMAGE.name())) {
                        container.remove(StatusType.CRITICAL_DAMAGE.getNamespacedKey());
                    }
                    if(statusType.equals(StatusType.CRITICAL)) {
                        container.set(StatusType.CRITICAL.getNamespacedKey(), PersistentDataType.DOUBLE, value);
                    }
                    if(statusType.equals(StatusType.CRITICAL_DAMAGE)) {
                        container.set(StatusType.CRITICAL.getNamespacedKey(), PersistentDataType.DOUBLE, value);
                    }

                    item.setItemMeta(itemMeta);
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
