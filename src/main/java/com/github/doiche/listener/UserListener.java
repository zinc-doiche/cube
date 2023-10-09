package com.github.doiche.listener;

import com.github.doiche.object.User;
import com.github.doiche.object.status.StatusType;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import net.kyori.adventure.text.Component;
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
        Entity entity = event.getTarget().getBukkitEntity();
        if(entity instanceof Player) {
            User user = User.getUser(entity.getUniqueId().toString());
            if(user == null) {
                return;
            }
            if(user.hasStatus(StatusType.DEFENSIVE_POWER)) {
                double defense = user.getStatus(StatusType.DEFENSIVE_POWER).getValue() / 100;
                if(defense > 1) {
                    defense = 1;
                }
                event.setDamage(event.getDamage() * (1 - defense));
                entity.sendMessage("받은 데미지: " + event.getDamage());
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof Player) {
            User user = User.getUser(entity.getUniqueId().toString());
            if(user == null) {
                return;
            }
            if(user.hasStatus(StatusType.DEFENSIVE_POWER)) {
                double defense = user.getStatus(StatusType.DEFENSIVE_POWER).getValue() / 100;
                if(defense > 1) {
                    defense = 1;
                }
                event.setDamage(event.getDamage() * (1 - defense));
                entity.sendMessage("받은 데미지: " + event.getFinalDamage());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if(entity instanceof Player) {
            User user = User.getUser(entity.getUniqueId().toString());
            if(user == null) {
                return;
            }
            //근접공격 피격 시
            if(damager instanceof LivingEntity) {
                entity.sendMessage(damager.getName() + "에게 근접공격 피격: " + event.getFinalDamage());
            }
            //원거리공격 피격 시
            else if(damager instanceof Projectile projectile) {
                ProjectileSource shooter = projectile.getShooter();
                if(shooter instanceof LivingEntity livingEntity) {
                    entity.sendMessage(livingEntity.getName() + "에게 원거리공격 피격: " + event.getFinalDamage());
                }
            }
        }

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
        if(user.hasStatus(StatusType.CRITICAL)) {
            double criticalValue = 1.5;
            if(user.hasStatus(StatusType.CRITICAL_DAMAGE)) {
                criticalValue += user.getStatus(StatusType.CRITICAL_DAMAGE).getValue() / 100;
            }
            if(user.getStatus(StatusType.CRITICAL).getValue() < Math.random() * 100) {
                event.setDamage(event.getDamage() * criticalValue);
            }
        }
    }
}
