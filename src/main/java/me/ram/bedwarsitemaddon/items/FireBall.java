package me.ram.bedwarsitemaddon.items;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.LocationUtil;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;

public class FireBall implements Listener {

    private final Map<Player, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        for (Player player : e.getGame().getPlayers()) {
            cooldown.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractFireball(PlayerInteractEvent e) {
        if (!Config.items_fireball_enabled) {
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
            if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && e.getItem().getType() == new ItemStack(Material.FIREBALL).getType()) {
                if ((System.currentTimeMillis() - cooldown.getOrDefault(player, (long) 0)) <= Config.items_fireball_cooldown * 1000) {
                    e.setCancelled(true);
                    player.sendMessage(Config.message_cooling.replace("{time}", String.format("%.1f", (((Config.items_fireball_cooldown * 1000 - System.currentTimeMillis() + cooldown.getOrDefault(player, (long) 0)) / 1000))) + ""));
                } else {
                    ItemStack stack = e.getItem();
                    BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.FIRE_BALL, stack);
                    Bukkit.getPluginManager().callEvent(bedwarsUseItemEvent);
                    if (!bedwarsUseItemEvent.isCancelled()) {
                        cooldown.put(player, System.currentTimeMillis());
                        Fireball fireball = player.launchProjectile(Fireball.class);
                        fireball.setYield((float) Config.items_fireball_range);
                        fireball.setBounce(false);
                        fireball.setShooter(player);
                        fireball.setMetadata("FireBall", new FixedMetadataValue(Main.getInstance(), game.getName() + "." + player.getName()));
                        TakeItemUtil.TakeItem(player, stack);
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockIgnite(BlockIgniteEvent e) {
        Entity entity = e.getIgnitingEntity();
        if (entity instanceof Fireball && entity.hasMetadata("FireBall")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent e) {
        if (!Config.items_fireball_enabled) {
            return;
        }
        Entity entity = e.getEntity();
        Entity damager = e.getDamager();
        if (!damager.hasMetadata("FireBall")) {
            return;
        }
        if (!(entity instanceof Player && damager instanceof Fireball)) {
            return;
        }
        Player player = (Player) entity;
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null) {
            return;
        }
        if (game.getState() != GameState.RUNNING) {
            return;
        }
        e.setDamage(Config.items_fireball_damage);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (!Config.items_fireball_enabled) {
            return;
        }
        Entity entity = e.getEntity();
        if (!(entity instanceof Fireball) || !entity.hasMetadata("FireBall")) {
            return;
        }
        Fireball fireball = (Fireball) e.getEntity();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
            if (game != null && game.getState() == GameState.RUNNING && !game.isSpectator(player) && player.getGameMode() != GameMode.SPECTATOR && player.getGameMode() != GameMode.CREATIVE) {
                if (e.getEntity().getWorld() == player.getWorld()) {
                    if (player.getLocation().distanceSquared((e.getEntity().getLocation())) <= Math.pow((fireball.getYield() + 1), 2)) {
                        if (fireball.getShooter() != null && ((Player) fireball.getShooter()).getUniqueId().equals(player.getUniqueId())) {
                            player.damage(Config.items_fireball_damage, fireball);
                        }
                        if (Config.items_fireball_ejection_enabled) {
                            player.setVelocity(LocationUtil.getPosition(player.getLocation(), fireball.getLocation(), 3).multiply(Config.items_fireball_ejection_velocity));
                            if (Config.items_fireball_ejection_no_fall) {
                                Main.getInstance().getNoFallManage().addPlayer(player);
                            }
                        }
                    }
                }
            }
        }
    }
}
