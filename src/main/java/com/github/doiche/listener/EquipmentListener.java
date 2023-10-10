package com.github.doiche.listener;

import com.github.doiche.object.User;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

public class EquipmentListener implements Listener {
    @EventHandler
    public void onEquip(PlayerInventorySlotChangeEvent event) {
        if(event.getRawSlot() < 5 || event.getRawSlot() > 8) {
            return;
        }
        Player player = event.getPlayer();
        User user = User.getUser(player.getUniqueId().toString());
        if(user == null) {
            return;
        }
        ItemStack oldItem = event.getOldItemStack();
        ItemStack newItem = event.getNewItemStack();
        PersistentDataContainer oldContainer;
        PersistentDataContainer newContainer;

        if(newItem.getItemMeta() != null && oldItem.getItemMeta() != null) {
            oldContainer = oldItem.getItemMeta().getPersistentDataContainer();
            newContainer = newItem.getItemMeta().getPersistentDataContainer();
            if(oldItem.getType() == newItem.getType() && oldContainer.equals(newContainer)) {
                return;
            }
        }

        //player.sendMessage("old:" + oldItem.getType() + " new:" + newItem.getType());
        if(!oldItem.isEmpty()) {
            oldContainer = oldItem.getItemMeta().getPersistentDataContainer();
            user.onDisarm(player, oldContainer);
            player.performCommand("status info");
        }
        if(!newItem.isEmpty()) {
            newContainer = newItem.getItemMeta().getPersistentDataContainer();
            user.onEquip(player, newContainer);
            player.performCommand("status info");
        }
    }
}
