package me.ram.bedwarsitemaddon.manage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class NoFallManage implements Listener {

    private final Map<UUID, Long> players;
    private final Map<UUID, BukkitTask> tasks;

    public NoFallManage() {
        players = new HashMap<>();
        tasks = new HashMap<>();
    }

    public void addPlayer(Player player) {
        players.put(player.getUniqueId(), System.currentTimeMillis());
        removeTask(player);
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        removeTask(player);
    }

    public void removeTask(Player player) {
        if (tasks.containsKey(player.getUniqueId())) {
            tasks.get(player.getUniqueId()).cancel();
            tasks.remove(player.getUniqueId());
        }
    }

    public boolean isNoFall(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    public boolean isJust(Player player) {
        return (System.currentTimeMillis() - players.getOrDefault(player.getUniqueId(), 0L)) <= 250;
    }

    public void addTask(Player player, BukkitTask task) {
        tasks.put(player.getUniqueId(), task);
    }
}
