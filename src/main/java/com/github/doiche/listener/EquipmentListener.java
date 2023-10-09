package com.github.doiche.listener;

import com.github.doiche.object.User;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
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
        User user = User.getUser(event.getPlayer().getUniqueId().toString());
        if(user == null) {
            return;
        }
        ItemStack oldItem = event.getOldItemStack();
        ItemStack newItem = event.getNewItemStack();

        if(!oldItem.isEmpty()) {
            PersistentDataContainer container = oldItem.getItemMeta().getPersistentDataContainer();
            user.onDisarm(container);
        }
        if(!newItem.isEmpty()) {
            PersistentDataContainer container = newItem.getItemMeta().getPersistentDataContainer();
            user.onEquip(container);
        }
    }
}
