package me.ram.bedwarsitemaddon.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameOverEvent;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsFishSpawnEvent;

public class TeamSilverFish implements Listener {
    private final Map<String, Map<Silverfish, Team>> Fishs = new HashMap<>();
    private final Map<String, Map<Silverfish, Integer>> Fishtime = new HashMap<>();

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        Game game = e.getGame();
        Fishs.put(game.getName(), new HashMap<>());
        Fishtime.put(game.getName(), new HashMap<>());
        new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getState() == GameState.RUNNING) {
                    Map<Silverfish, Integer> nmap = new HashMap<>();
                    Map<Silverfish, Integer> guardtime = Fishtime.get(game.getName());
                    List<Silverfish> removelist = new ArrayList<>();
                    for (Silverfish silverfish : guardtime.keySet()) {
                        if (guardtime.get(silverfish) == 0) {
                            silverfish.remove();
                            removelist.add(silverfish);
                        } else if (guardtime.get(silverfish) > 0) {
                            nmap.put(silverfish, guardtime.get(silverfish) - 1);
                        }
                    }
                    for (Silverfish silverfish : removelist) {
                        guardtime.remove(silverfish);
                    }
                    Fishtime.put(game.getName(), nmap);
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent e) {
        Entity entity = e.getEntity();
        if (!(e.getEntity() instanceof Snowball)) {
            return;
        }
        Snowball snowball = (Snowball) entity;
        if (!(snowball.getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) snowball.getShooter();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null) {
            return;
        }
        if (game.getState() != GameState.RUNNING) {
            return;
        }
        if (game.isSpectator(player)) {
            return;
        }
        if (game.getPlayerTeam(player) == null) {
            return;
        }
        BedwarsFishSpawnEvent bedwarsFishSpawnEvent = new BedwarsFishSpawnEvent(game, snowball.getLocation());
        Bukkit.getPluginManager().callEvent(bedwarsFishSpawnEvent);
        if (!bedwarsFishSpawnEvent.isCancelled()) {
            SpawnSilverfish(player, snowball.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent e) {
        if (Config.items_team_silver_fish_enabled && e.getEntity() instanceof Silverfish) {
            e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamagePlayerBySilverfish(EntityDamageByEntityEvent e) {
        if (!Config.items_team_silver_fish_enabled) {
            return;
        }
        if (!(e.getDamager() instanceof Silverfish && e.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getEntity();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null) {
            return;
        }
        if (!(game.getState() == GameState.RUNNING)) {
            return;
        }
        if (game.isSpectator(player)) {
            return;
        }
        Silverfish silverfish = (Silverfish) e.getDamager();
        Team team = Fishs.get(game.getName()).get(silverfish);
        if (team == null) {
            return;
        }
        if (team.getName().equals(game.getPlayerTeam(player).getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (e.getEntity() instanceof Silverfish) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageSilverfish(EntityDamageByEntityEvent e) {
        if (!Config.items_team_silver_fish_enabled) {
            return;
        }
        if (!(e.getDamager() instanceof Player && e.getEntity() instanceof Silverfish)) {
            return;
        }
        Player player = (Player) e.getDamager();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null) {
            return;
        }
        if (!(game.getState() == GameState.RUNNING)) {
            return;
        }
        if (game.isSpectator(player)) {
            return;
        }
        Silverfish silverfishe = (Silverfish) e.getEntity();
        if (!Fishs.get(game.getName()).containsKey(silverfishe)) {
            return;
        }
        if (Fishs.get(game.getName()).get(silverfishe) == game.getPlayerTeam(player)) {
            return;
        }
        e.setCancelled(false);
    }

    @EventHandler
    public void onDamagePlayer(EntityDamageByEntityEvent e) {
        if (!Config.items_team_silver_fish_enabled) {
            return;
        }
        if (!(e.getDamager() instanceof Silverfish && e.getEntity() instanceof Player)) {
            return;
        }
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer((Player) e.getEntity());
        if (game == null) {
            return;
        }
        if (game.getState() == GameState.RUNNING) {
            e.setDamage(Config.items_team_silver_fish_damage);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!Config.items_team_silver_fish_enabled) {
            return;
        }
        if (e.getEntity() instanceof Silverfish) {
            for (Map<Silverfish, Team> silverfishs : Fishs.values()) {
                if (silverfishs.containsKey((Silverfish) e.getEntity())) {
                    e.getDrops().clear();
                    e.setDroppedExp(0);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onOver(BedwarsGameOverEvent e) {
        for (Silverfish silverfish : Fishs.get(e.getGame().getName()).keySet()) {
            silverfish.remove();
        }
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin().equals(BedwarsRel.getInstance()) || e.getPlugin().equals(Main.getInstance())) {
            for (Map<Silverfish, Team> silverfishs : Fishs.values()) {
                for (Silverfish silverfish : silverfishs.keySet()) {
                    if (!silverfish.isDead()) {
                        silverfish.remove();
                    }
                }
            }
        }
    }

    public void SpawnSilverfish(Player player, Location location) {
        Silverfish silverfish = player.getWorld().spawn(location, Silverfish.class);
        silverfish.setMaxHealth(Config.items_team_silver_fish_health);
        silverfish.setHealth(silverfish.getMaxHealth());
        silverfish.setCustomNameVisible(true);
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        Fishs.get(game.getName()).put(silverfish, game.getPlayerTeam(player));
        Fishtime.get(game.getName()).put(silverfish, Config.items_team_silver_fish_staytime);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getState() == GameState.RUNNING) {
                    if (silverfish.isDead()) {
                        Fishtime.get(game.getName()).remove(silverfish);
                        Fishs.get(game.getName()).remove(silverfish);
                        cancel();
                        return;
                    }
                    silverfish.setCustomName(Config.items_team_silver_fish_name.replace("{color}", game.getPlayerTeam(player).getChatColor() + "").replace("{team}", game.getPlayerTeam(player).getName()).replace("{time}", (Fishtime.get(game.getName()).get(silverfish) + 1) + ""));
                    if (silverfish.getTarget() instanceof Player) {
                        Player target = (Player) silverfish.getTarget();
                        if (!target.isOnline()) {
                            silverfish.setTarget(null);
                        } else if (game.getPlayerTeam(target) == null) {
                            silverfish.setTarget(null);
                        } else if (game.isSpectator(target)) {
                            silverfish.setTarget(null);
                        } else if (target.getGameMode() == GameMode.SPECTATOR) {
                            silverfish.setTarget(null);
                        }
                    }
                    List<Player> players = new ArrayList<>();
                    for (Team team : game.getTeams().values()) {
                        if (team != game.getPlayerTeam(player)) {
                            players.addAll(team.getPlayers());
                        }
                    }
                    Player targetplayer = null;
                    double rangeSquared = 0;
                    for (Player p : players) {
                        if (p.getGameMode() != GameMode.SPECTATOR && p.getLocation().getWorld() == silverfish.getLocation().getWorld()) {
                            if (targetplayer == null) {
                                targetplayer = p;
                                rangeSquared = p.getLocation().distanceSquared(silverfish.getLocation());
                            } else if (rangeSquared > p.getLocation().distanceSquared(silverfish.getLocation())) {
                                targetplayer = p;
                                rangeSquared = p.getLocation().distanceSquared(silverfish.getLocation());
                            }
                        }
                    }
                    silverfish.setTarget(targetplayer);
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 0L);
    }
}
