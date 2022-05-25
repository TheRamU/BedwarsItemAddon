package me.ram.bedwarsitemaddon.items;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.ResourceSpawner;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.SoundMachine;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;
import me.ram.bedwarsscoreboardaddon.utils.BedwarsUtil;

public class CompactTower implements Listener {

    private final Map<Player, Long> cooldown = new HashMap<>();
    private List<List<String>> blocks;

    public CompactTower() {
        blocks = new ArrayList<>();
        loadBlocks();
    }

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        for (Player player : e.getGame().getPlayers()) {
            cooldown.remove(player);
        }
    }

    private void loadBlocks() {
        blocks = new ArrayList<>();
        try {
            URL url = Main.getInstance().getClass().getResource("/Tower.blocks");
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferReader.readLine()) != null) {
                List<String> list = new ArrayList<>();
                for (String l : line.split(";")) {
                    try {
                        list.add(l);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                blocks.add(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (!Config.items_compact_tower_enabled) {
            return;
        }
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (e.getItemInHand() == null || game == null) {
            return;
        }
        if (game.isOverSet() || game.getState() != GameState.RUNNING || game.isSpectator(player)) {
            return;
        }
        Team team = game.getPlayerTeam(player);
        if (team == null) {
            return;
        }
        if (e.getItemInHand().getType() != new ItemStack(Material.valueOf(Config.items_compact_tower_item)).getType()) {
            return;
        }
        e.setCancelled(true);
        if ((System.currentTimeMillis() - cooldown.getOrDefault(player, (long) 0)) <= Config.items_compact_tower_cooldown * 1000) {
            player.sendMessage(Config.message_cooling.replace("{time}", String.format("%.1f", (((Config.items_compact_tower_cooldown * 1000 - System.currentTimeMillis() + cooldown.getOrDefault(player, (long) 0)) / 1000))) + ""));
            return;
        }
        ItemStack stack = e.getItemInHand();
        BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.BRIDGE_EGG, stack);
        Bukkit.getPluginManager().callEvent(bedwarsUseItemEvent);
        if (!bedwarsUseItemEvent.isCancelled()) {
            cooldown.put(player, System.currentTimeMillis());
            setblock(game, team, e.getBlock().getLocation(), player);
            TakeItemUtil.TakeItem(player, stack);
        }
    }

    public void setblock(Game game, Team team, Location location, Player player) {
        int face = getFace(player.getLocation());
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (!game.getState().equals(GameState.RUNNING) || game.isOverSet() || i >= blocks.size()) {
                    cancel();
                    return;
                }
                Location loc = null;
                for (String line : blocks.get(i)) {
                    String[] ary = line.split(",");
                    try {
                        int x = Integer.valueOf(ary[0]);
                        int y = Integer.valueOf(ary[1]);
                        int z = Integer.valueOf(ary[2]);
                        if (face == 0) {
                            loc = location.clone().add(x, y, z);
                        } else if (face == 1) {
                            loc = location.clone().add(-z, y, x);
                        } else if (face == 2) {
                            loc = location.clone().add(-x, y, -z);
                        } else if (face == 3) {
                            loc = location.clone().add(z, y, -x);
                        }
                        Block block = loc.getBlock();
                        if (!isCanPlace(game, loc)) {
                            continue;
                        }
                        try {
                            Material type = Material.valueOf(ary[3]);
                            if (type == Material.WOOL) {
                                block.setType(Material.WOOL);
                                block.setData(team.getColor().getDyeColor().getWoolData());
                            } else if (type == Material.LADDER) {
                                block.setType(Material.LADDER);
                                if (face == 0) {
                                    block.setData((byte) 2);
                                } else if (face == 1) {
                                    block.setData((byte) 5);
                                } else if (face == 2) {
                                    block.setData((byte) 3);
                                } else if (face == 3) {
                                    block.setData((byte) 4);
                                }
                            } else {
                                block.setType(type);
                                try {
                                    block.setData(Byte.valueOf(ary[4]));
                                } catch (Exception ignored) {
                                }
                            }
                            game.getRegion().addPlacedBlock(block, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (loc != null) {
                    loc.getWorld().playSound(loc, SoundMachine.get("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG"), 5, 1);
                }
                i++;
            }
        }.runTaskTimer(Main.getInstance(), 0, 3L);
    }

    private boolean isCanPlace(Game game, Location location) {
        Block block = location.getBlock();
        if (!block.getType().equals(Material.AIR)) {
            return false;
        }
        if (!game.getRegion().isInRegion(location)) {
            return false;
        }
        for (Entity entity : location.getWorld().getNearbyEntities(location.clone().add(0.5, 1, 0.5), 0.5, 1, 0.5)) {
            if (!(entity instanceof Player)) {
                continue;
            }
            Player player = (Player) entity;
            if (!game.isInGame(player) || game.isSpectator(player)) {
                continue;
            }
            if (Bukkit.getPluginManager().isPluginEnabled("BedwarsScoreBoardAddon") && BedwarsUtil.isRespawning(player)) {
                continue;
            }
            return false;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("BedwarsScoreBoardAddon")) {
            if (me.ram.bedwarsscoreboardaddon.config.Config.spawn_no_build_spawn_enabled) {
                for (Team team : game.getTeams().values()) {
                    if (team.getSpawnLocation().distanceSquared(block.getLocation().clone().add(0.5, 0, 0.5)) <= Math.pow(me.ram.bedwarsscoreboardaddon.config.Config.spawn_no_build_spawn_range, 2)) {
                        return false;
                    }
                }
            }
            if (me.ram.bedwarsscoreboardaddon.config.Config.spawn_no_build_resource_enabled) {
                for (ResourceSpawner spawner : game.getResourceSpawners()) {
                    if (spawner.getLocation().distanceSquared(block.getLocation().clone().add(0.5, 0, 0.5)) <= Math.pow(me.ram.bedwarsscoreboardaddon.config.Config.spawn_no_build_resource_range, 2)) {
                        return false;
                    }
                }
                if (me.ram.bedwarsscoreboardaddon.config.Config.game_team_spawner.containsKey(game.getName())) {
                    for (List<Location> locs : me.ram.bedwarsscoreboardaddon.config.Config.game_team_spawner.get(game.getName()).values()) {
                        for (Location loc : locs) {
                            if (loc.distanceSquared(block.getLocation().clone().add(0.5, 0, 0.5)) <= Math.pow(me.ram.bedwarsscoreboardaddon.config.Config.spawn_no_build_resource_range, 2)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private int getFace(Location location) {
        List<Integer> list = new ArrayList<>();
        for (int i = -360; i <= 360; i += 90) {
            list.add(i);
        }
        int yaw = (int) location.getYaw();
        int a = Math.abs(list.get(0) - yaw);
        int nyaw = list.get(0);
        for (int i : list) {
            int j = Math.abs(i - yaw);
            if (j < a) {
                a = j;
                nyaw = i;
            }
        }
        int face = 0;
        if (nyaw == -360 || nyaw == 0 || nyaw == 360) {
            face = 0;
        } else if (nyaw == 90 || nyaw == -270) {
            face = 1;
        } else if (nyaw == 180 || nyaw == -180) {
            face = 2;
        } else if (nyaw == 270 || nyaw == -90) {
            face = 3;
        }
        return face;
    }
}
