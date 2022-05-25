package me.ram.bedwarsitemaddon.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameOverEvent;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;

public class WalkPlatform implements Listener {

    private final Map<Player, Long> cooldown = new HashMap<>();
    private final Map<String, Map<Block, BukkitTask>> blocktasks = new HashMap<>();
    private final Map<String, Map<Player, BukkitTask>> tasks = new HashMap<>();

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        for (Player player : e.getGame().getPlayers()) {
            cooldown.remove(player);
        }
        blocktasks.put(e.getGame().getName(), new HashMap<>());
        tasks.put(e.getGame().getName(), new HashMap<>());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!Config.items_walk_platform_enabled) {
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
        if (game.isSpectator(player)) {
            return;
        }
        if (game.getState() == GameState.RUNNING) {
            if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && e.getItem().getType() == Material.valueOf(Config.items_walk_platform_item)) {
                e.setCancelled(true);
                if ((System.currentTimeMillis() - cooldown.getOrDefault(player, (long) 0)) <= Config.items_walk_platform_cooldown * 1000) {
                    player.sendMessage(Config.message_cooling.replace("{time}", String.format("%.1f", (((Config.items_walk_platform_cooldown * 1000 - System.currentTimeMillis() + cooldown.getOrDefault(player, (long) 0)) / 1000))) + ""));
                } else {
                    ItemStack stack = e.getItem();
                    BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.WALK_PLATFORM, stack);
                    Bukkit.getPluginManager().callEvent(bedwarsUseItemEvent);
                    if (!bedwarsUseItemEvent.isCancelled()) {
                        cooldown.put(player, System.currentTimeMillis());
                        TakeItemUtil.TakeItem(player, e.getItem());
                        if (tasks.get(game.getName()).containsKey(player)) {
                            tasks.get(game.getName()).get(player).cancel();
                        }
                        runPlatform(player, game, game.getPlayerTeam(player));
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    private void runPlatform(Player player, Game game, Team team) {
        BukkitTask bukkittask = new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i >= 20 * Config.items_walk_platform_break_time) {
                    this.cancel();
                    return;
                }
                i++;
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR || !game.getPlayers().contains(player) || game.isSpectator(player) || player.isSneaking()) {
                    this.cancel();
                    return;
                }
                Location location = player.getLocation().getBlock().getLocation().clone().add(0, -1, 0);
                List<Block> blocks = new ArrayList<>();
                blocks.add(location.clone().add(1, 0, 1).getBlock());
                blocks.add(location.clone().add(1, 0, 0).getBlock());
                blocks.add(location.clone().add(1, 0, -1).getBlock());
                blocks.add(location.clone().add(0, 0, 1).getBlock());
                blocks.add(location.clone().add(0, 0, 0).getBlock());
                blocks.add(location.clone().add(0, 0, -1).getBlock());
                blocks.add(location.clone().add(-1, 0, 1).getBlock());
                blocks.add(location.clone().add(-1, 0, 0).getBlock());
                blocks.add(location.clone().add(-1, 0, -1).getBlock());
                blocks.add(location.clone().add(2, 0, 1).getBlock());
                blocks.add(location.clone().add(2, 0, 0).getBlock());
                blocks.add(location.clone().add(2, 0, -1).getBlock());
                blocks.add(location.clone().add(-2, 0, 1).getBlock());
                blocks.add(location.clone().add(-2, 0, 0).getBlock());
                blocks.add(location.clone().add(-2, 0, -1).getBlock());
                blocks.add(location.clone().add(1, 0, 2).getBlock());
                blocks.add(location.clone().add(0, 0, 2).getBlock());
                blocks.add(location.clone().add(-1, 0, 2).getBlock());
                blocks.add(location.clone().add(1, 0, -2).getBlock());
                blocks.add(location.clone().add(0, 0, -2).getBlock());
                blocks.add(location.clone().add(-1, 0, -2).getBlock());
                for (Block block : blocks) {
                    if (block.getType() == Material.AIR && game.getRegion().isInRegion(block.getLocation())) {
                        game.getRegion().addPlacedBlock(block, null);
                        block.setType(Material.WOOL);
                        block.setData(team.getColor().getDyeColor().getWoolData());
                        blocktasks.get(game.getName()).put(block, new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (block.getType() == Material.WOOL) {
                                    block.setType(Material.AIR);
                                }
                                blocktasks.get(game.getName()).remove(block);
                            }
                        }.runTaskLater(Main.getInstance(), 1L));
                    } else if (blocktasks.get(game.getName()).containsKey(block)) {
                        blocktasks.get(game.getName()).get(block).cancel();
                        blocktasks.get(game.getName()).put(block, new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (block.getType() == Material.WOOL && block.getData() == team.getColor().getDyeColor().getWoolData()) {
                                    block.setType(Material.AIR);
                                }
                                blocktasks.get(game.getName()).remove(block);
                            }
                        }.runTaskLater(Main.getInstance(), 1L));
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
        game.addRunningTask(bukkittask);
        tasks.get(game.getName()).put(player, bukkittask);
    }

    @EventHandler
    public void onOver(BedwarsGameOverEvent e) {
        for (BukkitTask task : blocktasks.get(e.getGame().getName()).values()) {
            task.cancel();
        }
        for (BukkitTask task : tasks.get(e.getGame().getName()).values()) {
            task.cancel();
        }
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin().equals(BedwarsRel.getInstance()) || e.getPlugin().equals(Main.getInstance())) {
            for (Map<Block, BukkitTask> blocks : blocktasks.values()) {
                for (BukkitTask task : blocks.values()) {
                    task.cancel();
                }
            }
        }
    }
}
