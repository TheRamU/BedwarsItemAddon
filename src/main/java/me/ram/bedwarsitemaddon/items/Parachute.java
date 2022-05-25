package me.ram.bedwarsitemaddon.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameOverEvent;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.utils.SoundMachine;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.LocationUtil;
import me.ram.bedwarsitemaddon.utils.NMS;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;

public class Parachute implements Listener {
    private Map<String, List<ArmorStand>> armorstands = new HashMap<>();
    private final Map<Player, Long> cooldown = new HashMap<>();
    private final Map<String, Map<Player, Integer>> ejection = new HashMap<>();

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        for (Player player : e.getGame().getPlayers()) {
            cooldown.remove(player);
        }
        Game game = e.getGame();
        armorstands.put(game.getName(), new ArrayList<>());
        ejection.put(game.getName(), new HashMap<>());
    }

    @EventHandler
    public void onOver(BedwarsGameOverEvent e) {
        Game game = e.getGame();
        for (ArmorStand armorstand : armorstands.get(game.getName())) {
            if (!armorstand.isDead()) {
                armorstand.remove();
            }
        }
        armorstands.put(game.getName(), new ArrayList<>());
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin().equals(BedwarsRel.getInstance()) || e.getPlugin().equals(Main.getInstance())) {
            for (List<ArmorStand> as : armorstands.values()) {
                for (ArmorStand armorstand : as) {
                    if (!armorstand.isDead()) {
                        armorstand.remove();
                    }
                }
            }
            armorstands = new HashMap<>();
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerArmorStandManipulateEvent e) {
        if (!Config.items_parachute_enabled) {
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
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!Config.items_parachute_enabled) {
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
            if (e.getBlock().getType() == new ItemStack(Material.valueOf(Config.items_parachute_item)).getType()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!Config.items_parachute_enabled) {
            return;
        }
        Player player = e.getPlayer();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (e.getItem() == null || game == null) {
            return;
        }
        if (!game.getPlayers().contains(player)) {
            return;
        }
        if (game.getState() == GameState.RUNNING) {
            if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && e.getItem().getType() == new ItemStack(Material.valueOf(Config.items_parachute_item)).getType()) {
                if ((System.currentTimeMillis() - cooldown.getOrDefault(player, (long) 0)) <= Config.items_parachute_cooldown * 1000) {
                    e.setCancelled(true);
                    player.sendMessage(Config.message_cooling.replace("{time}", String.format("%.1f", (((Config.items_parachute_cooldown * 1000 - System.currentTimeMillis() + cooldown.getOrDefault(player, (long) 0)) / 1000))) + ""));
                } else {
                    ItemStack stack = e.getItem();
                    BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.PARACHUTE, stack);
                    Bukkit.getPluginManager().callEvent(bedwarsUseItemEvent);
                    if (!bedwarsUseItemEvent.isCancelled()) {
                        cooldown.put(player, System.currentTimeMillis());
                        ejection.get(game.getName()).put(player, ejection.get(game.getName()).getOrDefault(player, 0) + 1);
                        player.getWorld().playSound(player.getLocation(), SoundMachine.get("FIREWORK_LARGE_BLAST", "ENTITY_FIREWORK_LARGE_BLAST"), 1.0f, 1.0f);
                        player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION_LARGE, 1);
                        player.setSneaking(true);
                        this.setParachute(game, player);
                        TakeItemUtil.TakeItem(player, stack);
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    public void setParachute(Game game, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setSneaking(false);
                player.setVelocity(new Vector(0, Config.items_parachute_velocity, 0));
            }
        }.runTaskLater(Main.getInstance(), 1L);
        int ei = ejection.get(game.getName()).get(player);
        new BukkitRunnable() {
            boolean po = false;
            ArmorStand armorStand1;
            ArmorStand armorStand2;
            ArmorStand armorStand3;
            ArmorStand armorStand4;
            ArmorStand armorStand5;

            @Override
            public void run() {
                if (ei != ejection.get(game.getName()).get(player)) {
                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        Main.getInstance().getNoFallManage().removePlayer(player);
                    }
                    if (po) {
                        armorStand1.remove();
                        armorStand2.remove();
                        armorStand3.remove();
                        armorStand4.remove();
                        armorStand5.remove();
                    }
                    cancel();
                    return;
                }
                if (!player.isOnline()) {
                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        Main.getInstance().getNoFallManage().removePlayer(player);
                    }
                    if (po) {
                        armorStand1.remove();
                        armorStand2.remove();
                        armorStand3.remove();
                        armorStand4.remove();
                        armorStand5.remove();
                    }
                    ejection.get(game.getName()).put(player, 0);
                    cancel();
                    return;
                }
                if (player.isDead()) {
                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        Main.getInstance().getNoFallManage().removePlayer(player);
                    }
                    if (po) {
                        armorStand1.remove();
                        armorStand2.remove();
                        armorStand3.remove();
                        armorStand4.remove();
                        armorStand5.remove();
                    }
                    cancel();
                    return;
                }
                Main.getInstance().getNoFallManage().addPlayer(player);
                if (player.isSneaking()) {
                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        Main.getInstance().getNoFallManage().removePlayer(player);
                    }
                    if (po) {
                        armorStand1.remove();
                        armorStand2.remove();
                        armorStand3.remove();
                        armorStand4.remove();
                        armorStand5.remove();
                    }
                    player.setVelocity(new Vector(0, 0, 0));
                    cancel();
                    return;
                }
                if (!po) {
                    player.setVelocity(player.getVelocity());
                }
                if (!po && player.getVelocity().getY() < 0) {
                    World world = player.getWorld();
                    armorStand1 = world.spawn(LocationUtil.getLocationYaw(player.getLocation(), 0, world.getMaxHeight(), 0), ArmorStand.class);
                    armorStand1.setVisible(false);
                    armorStand1.setGravity(false);
                    armorStand1.setBasePlate(false);
                    armorStand1.setHelmet(new ItemStack(Material.CARPET));

                    armorStand2 = world.spawn(LocationUtil.getLocationYaw(player.getLocation(), 0.61, world.getMaxHeight(), 0), ArmorStand.class);
                    armorStand2.setVisible(false);
                    armorStand2.setGravity(false);
                    armorStand2.setBasePlate(false);
                    armorStand2.setHelmet(new ItemStack(Material.CARPET));
                    armorStand2.setHeadPose(EulerAngle.ZERO.setZ(0.1));

                    armorStand3 = world.spawn(LocationUtil.getLocationYaw(player.getLocation(), 1.2, world.getMaxHeight(), 0), ArmorStand.class);
                    armorStand3.setVisible(false);
                    armorStand3.setGravity(false);
                    armorStand3.setBasePlate(false);
                    armorStand3.setHelmet(new ItemStack(Material.CARPET));
                    armorStand3.setHeadPose(EulerAngle.ZERO.setZ(0.3));

                    armorStand4 = world.spawn(LocationUtil.getLocationYaw(player.getLocation(), -0.61, world.getMaxHeight(), 0), ArmorStand.class);
                    armorStand4.setVisible(false);
                    armorStand4.setGravity(false);
                    armorStand4.setBasePlate(false);
                    armorStand4.setHelmet(new ItemStack(Material.CARPET));
                    armorStand4.setHeadPose(EulerAngle.ZERO.setZ(-0.1));

                    armorStand5 = world.spawn(LocationUtil.getLocationYaw(player.getLocation(), -1.2, world.getMaxHeight(), 0), ArmorStand.class);
                    armorStand5.setVisible(false);
                    armorStand5.setGravity(false);
                    armorStand5.setBasePlate(false);
                    armorStand5.setHelmet(new ItemStack(Material.CARPET));
                    armorStand5.setHeadPose(EulerAngle.ZERO.setZ(-0.3));
                    po = true;
                    player.getWorld().playSound(player.getLocation(), SoundMachine.get("HORSE_ARMOR", "ENTITY_HORSE_ARMOR"), 1.0f, 1.0f);
                    armorstands.get(game.getName()).add(armorStand1);
                    armorstands.get(game.getName()).add(armorStand2);
                    armorstands.get(game.getName()).add(armorStand3);
                    armorstands.get(game.getName()).add(armorStand4);
                    armorstands.get(game.getName()).add(armorStand5);
                }
                if (player.getVelocity().getY() > 0 && po) {
                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        Main.getInstance().getNoFallManage().removePlayer(player);
                    }
                    if (po) {
                        armorStand1.remove();
                        armorStand2.remove();
                        armorStand3.remove();
                        armorStand4.remove();
                        armorStand5.remove();
                    }
                    cancel();
                    return;
                }
                if (player.getVelocity().getY() < 0) {
                    Vector vector = player.getLocation().getDirection().multiply(Config.items_parachute_gliding_velocity);
                    vector.setY(Config.items_parachute_landing_velocity * -1);
                    player.setVelocity(vector);
                }
                if (po) {
                    if (!armorStand1.getLocation().getBlock().getChunk().isLoaded()) {
                        armorStand1.getLocation().getBlock().getChunk().load(true);
                    }
                    if (!armorStand2.getLocation().getBlock().getChunk().isLoaded()) {
                        armorStand2.getLocation().getBlock().getChunk().load(true);
                    }
                    if (!armorStand3.getLocation().getBlock().getChunk().isLoaded()) {
                        armorStand3.getLocation().getBlock().getChunk().load(true);
                    }
                    if (!armorStand4.getLocation().getBlock().getChunk().isLoaded()) {
                        armorStand4.getLocation().getBlock().getChunk().load(true);
                    }
                    if (!armorStand5.getLocation().getBlock().getChunk().isLoaded()) {
                        armorStand5.getLocation().getBlock().getChunk().load(true);
                    }
                    NMS.teleportEntity(game, armorStand1, LocationUtil.getLocationYaw(player.getLocation(), 0, 2.5 - 0.5, 0));
                    NMS.teleportEntity(game, armorStand2, LocationUtil.getLocationYaw(player.getLocation(), 0.61, 2.47 - 0.5, 0));
                    NMS.teleportEntity(game, armorStand3, LocationUtil.getLocationYaw(player.getLocation(), 1.2, 2.35 - 0.5, 0));
                    NMS.teleportEntity(game, armorStand4, LocationUtil.getLocationYaw(player.getLocation(), -0.61, 2.47 - 0.5, 0));
                    NMS.teleportEntity(game, armorStand5, LocationUtil.getLocationYaw(player.getLocation(), -1.2, 2.35 - 0.5, 0));
                }
                Location blockloc = player.getLocation();
                blockloc = blockloc.add(0, -1, 0);
                Block block = blockloc.getBlock();
                if (player.isOnGround() || (block != null && block.getType() != Material.AIR)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.getGameMode() != GameMode.SPECTATOR) {
                                Main.getInstance().getNoFallManage().removePlayer(player);
                            }
                        }
                    }.runTaskLater(Main.getInstance(), 10L);
                    if (po) {
                        armorStand1.remove();
                        armorStand2.remove();
                        armorStand3.remove();
                        armorStand4.remove();
                        armorStand5.remove();
                    }
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(Main.getInstance(), 5L, 0L);
    }
}
