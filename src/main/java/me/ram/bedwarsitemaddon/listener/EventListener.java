package me.ram.bedwarsitemaddon.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import io.github.bedwarsrel.events.BedwarsGameOverEvent;
import io.github.bedwarsrel.events.BedwarsGameStartedEvent;
import io.github.bedwarsrel.events.BedwarsPlayerLeaveEvent;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.manage.NoFallManage;

public class EventListener implements Listener {

    @EventHandler
    public void onOver(BedwarsGameOverEvent e) {
        e.getGame().setOver(true);
    }

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent e) {
        e.getGame().getPlayers().forEach(player -> {
            Main.getInstance().getNoFallManage().removePlayer(player);
        });
    }

    @EventHandler
    public void onLeave(BedwarsPlayerLeaveEvent e) {
        Main.getInstance().getNoFallManage().removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        GameMode mode = e.getNewGameMode();
        if (mode.equals(GameMode.CREATIVE) || mode.equals(GameMode.SPECTATOR)) {
            return;
        }
        Main.getInstance().getNoFallManage().removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Main.getInstance().getNoFallManage().removePlayer(e.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Block block = e.getTo().clone().add(0, -1, 0).getBlock();
        if (!player.isOnGround() && (block == null || block.getType().equals(Material.AIR))) {
            return;
        }
        NoFallManage man = Main.getInstance().getNoFallManage();
        if (!man.isNoFall(player)) {
            return;
        }
        if (man.isJust(player)) {
            return;
        }
        man.addTask(player, Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            man.removePlayer(player);
        }, 10L));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (!e.getCause().equals(DamageCause.FALL)) {
            return;
        }
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getEntity();
        NoFallManage man = Main.getInstance().getNoFallManage();
        if (man.isNoFall(player)) {
            man.removePlayer(player);
            e.setCancelled(true);
        }
    }
}
