package com.github.doiche.listener;

import com.github.doiche.Main;
import com.github.doiche.object.User;
import com.github.doiche.object.status.StatusType;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.UUID;

public class UserListener implements Listener {
    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getPlayerProfile().getId();
        if(uuid == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("UUID is null"));
            return;
        }
        User.addUser(new User(uuid.toString()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        User.removeUser(event.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void onMythicDamage(MythicDamageEvent event) {
        if(event.getTarget().getBukkitEntity() instanceof Player player) {
            User user = User.getUser(player.getUniqueId().toString());
            if(user == null) {
                return;
            }
            if(user.hasStatus(StatusType.BLOCK)) {
                double block = user.getStatus(StatusType.BLOCK).getValue();
                if(block > 100) {
                    //invulnerable
                    event.setCancelled(true);
                    event.setDamage(.0);
                    return;
                }
                if(block > Math.random() * 100) {
                    event.setDamage(.0);
                    player.sendMessage("회피!");
                    player.playSound(Sound.sound()
                            .type(org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP)
                            .pitch(0.5f)
                            .volume(0.7f)
                            .build());
                    return;
                }
            }
            if(user.hasStatus(StatusType.DEFENSIVE_POWER)) {
                double defense = user.getStatus(StatusType.DEFENSIVE_POWER).getValue() / 100;
                if(defense > 1) {
                    defense = 1;
                }
                event.setDamage(event.getDamage() * (1 - defense));
            }
            player.sendMessage("받은 데미지: " + event.getDamage());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player player) {
            User user = User.getUser(player.getUniqueId().toString());
            if(user == null) {
                return;
            }
            if(user.hasStatus(StatusType.BLOCK)) {
                double block = user.getStatus(StatusType.BLOCK).getValue();
                if(block > 100) {
                    //invulnerable
                    event.setCancelled(true);
                    event.setDamage(.0);
                    return;
                }
                if(block > Math.random() * 100) {
                    event.setDamage(.0);
                    player.sendMessage("회피!");
                    player.playSound(Sound.sound()
                            .type(org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP)
                            .pitch(0.5f)
                            .volume(0.7f)
                            .build());
                    return;
                }
            }
            if(user.hasStatus(StatusType.DEFENSIVE_POWER)) {
                double defense = user.getStatus(StatusType.DEFENSIVE_POWER).getValue() / 100;
                if(defense > 1) {
                    defense = 1;
                }
                event.setDamage(event.getDamage() * (1 - defense));
            }
            player.sendMessage("받은 데미지: " + event.getFinalDamage());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if(entity instanceof LivingEntity) {
            //근접공격 타격 시
            if(damager instanceof Player player) {
                rollCritical(player, event);
                player.sendMessage(entity.getName() + "에게 근접공격 타격: " + event.getFinalDamage());
            }
            //원거리공격 타격 시
            else if(damager instanceof Projectile projectile) {
                ProjectileSource shooter = projectile.getShooter();
                if(shooter instanceof Player player) {
                    rollCritical(player, event);
                    player.sendMessage(entity.getName() + "에게 원거리공격 타격: " + event.getFinalDamage());
                }
            }
        }
    }

    private void rollCritical(Player player, EntityDamageByEntityEvent event) {
        User user = User.getUser(player.getUniqueId().toString());
        if(user == null) {
            return;
        }
        if(!user.hasStatus(StatusType.CRITICAL)) {
            return;
        }
        double criticalValue = 1.5;
        double criticalProbability = user.getStatus(StatusType.CRITICAL).getValue();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.hasItemMeta()) {
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if(user.hasStatus(StatusType.CRITICAL_DAMAGE)) {
                criticalValue += user.getStatus(StatusType.CRITICAL_DAMAGE).getValue() / 100;
            }
            if(container.has(StatusType.CRITICAL.getNamespacedKey())) {
                Double value = container.get(StatusType.CRITICAL.getNamespacedKey(), PersistentDataType.DOUBLE);
                criticalProbability += value;
            }
            if(container.has(StatusType.CRITICAL_DAMAGE.getNamespacedKey())) {
                Double value = container.get(StatusType.CRITICAL_DAMAGE.getNamespacedKey(), PersistentDataType.DOUBLE);
                criticalValue += value;
            }
        }
        if(criticalProbability > Math.random() * 100) {
            player.sendMessage(Component.text("치명타!")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, true));
            player.playSound(Sound.sound()
                    .type(org.bukkit.Sound.ENTITY_IRON_GOLEM_DEATH)
                    .pitch(1.5f)
                    .volume(0.3f)
                    .build());
            event.setDamage(event.getDamage() * criticalValue);
        }
    }
}
