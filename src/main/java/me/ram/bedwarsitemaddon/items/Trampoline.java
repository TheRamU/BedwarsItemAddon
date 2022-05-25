package me.ram.bedwarsitemaddon.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameOverEvent;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.LocationUtil;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;

public class Trampoline implements Listener {

    private final Map<String, List<Location>> vblocks = new HashMap<>();
    private Map<String, List<Location>> blocks = new HashMap<>();
    private final Map<Player, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onOver(BedwarsGameOverEvent e) {
        Game game = e.getGame();
        for (Location location : blocks.get(game.getName())) {
            location.getBlock().setType(Material.AIR);
        }
        blocks.put(game.getName(), new ArrayList<>());
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin().equals(BedwarsRel.getInstance()) || e.getPlugin().equals(Main.getInstance())) {
            for (List<Location> locations : blocks.values()) {
                for (Location location : locations) {
                    location.getBlock().setType(Material.AIR);
                }
            }
            blocks = new HashMap<>();
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!Config.items_trampoline_enabled) {
            return;
        }
        Player player = e.getPlayer();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null) {
            return;
        }
        if (!game.getPlayers().contains(player)) {
            return;
        }
        if (game.getState() == GameState.RUNNING) {
            if (e.getBlock().getType() == new ItemStack(Material.valueOf(Config.items_trampoline_item)).getType()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!Config.items_trampoline_enabled) {
            return;
        }
        Player player = e.getPlayer();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (e.getItem() == null || game == null) {
            return;
        }
        if (game.isOverSet()) {
            return;
        }
        if (!game.getPlayers().contains(player)) {
            return;
        }
        if (game.getState() == GameState.RUNNING) {
            if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && e.getItem().getType() == new ItemStack(Material.valueOf(Config.items_trampoline_item)).getType()) {
                if ((System.currentTimeMillis() - cooldown.getOrDefault(player, (long) 0)) <= Config.items_trampoline_cooldown * 1000) {
                    e.setCancelled(true);
                    player.sendMessage(Config.message_cooling.replace("{time}", String.format("%.1f", (((Config.items_trampoline_cooldown * 1000 - System.currentTimeMillis() + cooldown.getOrDefault(player, (long) 0)) / 1000))) + ""));
                } else {
                    ItemStack stack = e.getItem();
                    Location location1 = player.getLocation();
                    Location location2 = player.getLocation();
                    int r = 0;
                    if (Config.items_trampoline_size <= 1) {
                        r = 3;
                    } else if (Config.items_trampoline_size == 2) {
                        r = 4;
                    } else if (Config.items_trampoline_size >= 3) {
                        r = 5;
                    }
                    location1.add(r, 0, r);
                    location2.add(-r, 1, -r);
                    if (this.isEnoughSpace(location1, location2)) {
                        BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.TRAMPOLINE, stack);
                        Bukkit.getPluginManager().callEvent(bedwarsUseItemEvent);
                        if (!bedwarsUseItemEvent.isCancelled()) {
                            cooldown.put(player, System.currentTimeMillis());
                            this.setTrampolineBlock(game, player.getLocation(), player, Config.items_trampoline_size);
                            player.teleport(player.getLocation().add(0, 2, 0));
                            player.setVelocity(new Vector(0, Config.items_trampoline_velocity, 0));
                            TakeItemUtil.TakeItem(player, stack);
                        }
                    } else {
                        player.sendMessage(Config.items_trampoline_lack_space);
                        return;
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    private void setTrampolineBlock(Game game, Location location, Player player, int size) {
        if (size <= 1) {
            this.setBlock(game, LocationUtil.getLocation(location, 2, 0, 2), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 0, 2), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 0, -2), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 0, -2), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 0), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 0), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, -1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, -1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, -2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, -2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, -2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, -2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, -2), Material.WOOL, (byte) 11);
        } else if (size == 2) {
            this.setBlock(game, LocationUtil.getLocation(location, 3, 0, 3), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 0, 3), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 0, -3), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 0, -3), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, 3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, 3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, 2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, 1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, 0), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, -1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, -2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, 2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, 1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, 0), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, -1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, -2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, -3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, -3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, -3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, -3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, -3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, -3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, -3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, -2), Material.WOOL, (byte) 15);
        } else if (size >= 3) {
            this.setBlock(game, LocationUtil.getLocation(location, 4, 0, 4), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 0, 4), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 0, -4), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 0, -4), Material.FENCE, (byte) 0);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 1, 4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, 4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, 4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 1, 4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 1, 3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 1, 2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 1, 1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 1, 0), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 1, -1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 1, -2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 1, -3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 4, 1, -4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 1, 3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 1, 2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 1, 1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 1, 0), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 1, -1), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 1, -2), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 1, -3), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, -4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, -4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, -4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, -4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, -4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, -4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, -4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, -4, 1, -4), Material.WOOL, (byte) 11);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, 3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, 3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 3, 1, -3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, -3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, -3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, -3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, -3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, -3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -3, 1, -3), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, 0), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, -1), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 2, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 1, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, 0, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -1, 1, -2), Material.WOOL, (byte) 15);
            this.setBlock(game, LocationUtil.getLocation(location, -2, 1, -2), Material.WOOL, (byte) 15);
        }
        Main.getInstance().getNoFallManage().addPlayer(player);
    }

    private void setBlock(Game game, Location location, Material material, byte data) {
        Location loc = location.getBlock().getLocation();
        for (Player player : LocationUtil.getLocationPlayers(loc)) {
            player.teleport(player.getLocation().add(0, 2, 0));
        }
        for (Player player : LocationUtil.getLocationPlayers(loc.add(0, -1, 0))) {
            player.teleport(player.getLocation().add(0, 1, 0));
        }
        Block block = location.getBlock();
        block.setType(material);
        block.setData(data);
        blocks.get(game.getName()).add(location);
        if (data == 15 || data == 11) {
            vblocks.get(game.getName()).add(location);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(Material.AIR);
                vblocks.get(game.getName()).remove(location);
                blocks.get(game.getName()).remove(location);
            }
        }.runTaskLater(Main.getInstance(), Config.items_trampoline_staytime * 20);
    }

    private boolean isEnoughSpace(Location location1, Location location2) {
        boolean enough = true;
        Location location = location1.getBlock().getLocation();
        for (int X : this.getAllNumber((int) location1.getX(), (int) location2.getX())) {
            location.setX(X);
            for (int Y : this.getAllNumber((int) location1.getY(), (int) location2.getY())) {
                location.setY(Y);
                for (int Z : this.getAllNumber((int) location1.getZ(), (int) location2.getZ())) {
                    location.setZ(Z);
                    if (location.getBlock() != null) {
                        if (location.getBlock().getType() != Material.AIR) {
                            enough = false;
                        }
                    }
                }
            }
        }
        return enough;
    }

    private List<Integer> getAllNumber(int a, int b) {
        List<Integer> nums = new ArrayList<>();
        int min = a;
        int max = b;
        if (a > b) {
            min = b;
            max = a;
        }
        for (int i = min; i < max + 1; i++) {
            nums.add(i);
        }
        return nums;
    }

    @EventHandler
    private void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getEntity();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null) {
            return;
        }
        if (game.getState() != GameState.RUNNING) {
            return;
        }
        if (!game.getPlayers().contains(player)) {
            return;
        }
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            Location location = player.getLocation().add(0, -1, 0);
            for (Location b : vblocks.get(game.getName())) {
                if (b.clone().add(0.5, 0, 0.5).distanceSquared(location) < 1) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void onStart(BedwarsGameStartEvent e) {
        for (Player player : e.getGame().getPlayers()) {
            cooldown.remove(player);
        }
        Game game = e.getGame();
        vblocks.put(game.getName(), new ArrayList<>());
        blocks.put(game.getName(), new ArrayList<>());
        new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getState() != GameState.RUNNING) {
                    cancel();
                    return;
                }
                for (Player player : game.getPlayers()) {
                    if (!game.isSpectator(player)) {
                        Location location = player.getLocation().add(0, -1, 0);
                        for (Location b : vblocks.get(game.getName())) {
                            if (b.clone().add(0.5, 0, 0.5).distanceSquared(location) < 1) {
                                player.setVelocity(new Vector(0, Config.items_trampoline_velocity, 0));
                                Main.getInstance().getNoFallManage().addPlayer(player);
                                break;
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 0L);
    }
}
