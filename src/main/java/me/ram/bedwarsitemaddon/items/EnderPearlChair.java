package me.ram.bedwarsitemaddon.items;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;

public class EnderPearlChair implements Listener {
    private final Map<Player, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        for (Player player : e.getGame().getPlayers()) {
            cooldown.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (!Config.items_ender_pearl_chair_enabled) {
            return;
        }
        Player player = e.getPlayer();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (e.getItem() == null || game == null) {
            return;
        }
        if (game.getState() != GameState.RUNNING) {
            return;
        }
        if (!game.getPlayers().contains(player)) {
            return;
        }
        if (game.getState() == GameState.RUNNING) {
            if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && e.getItem().getType() == new ItemStack(Material.ENDER_PEARL).getType()) {
                if ((System.currentTimeMillis() - cooldown.getOrDefault(player, (long) 0)) <= Config.items_ender_pearl_chair_cooldown * 1000) {
                    e.setCancelled(true);
                    player.sendMessage(Config.message_cooling.replace("{time}", String.format("%.1f", (((Config.items_ender_pearl_chair_cooldown * 1000 - System.currentTimeMillis() + cooldown.getOrDefault(player, (long) 0)) / 1000))) + ""));
                } else {
                    ItemStack stack = e.getItem();
                    BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.ENDER_PEARL_CHAIR, stack);
                    Bukkit.getPluginManager().callEvent(bedwarsUseItemEvent);
                    if (!bedwarsUseItemEvent.isCancelled()) {
                        cooldown.put(player, System.currentTimeMillis());
                        EnderPearl enderpearl = player.launchProjectile(EnderPearl.class);
                        enderpearl.setShooter(player);
                        enderpearl.setPassenger(player);
                        this.removeEnderPearl(player, enderpearl);
                        TakeItemUtil.TakeItem(player, stack);
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    public void removeEnderPearl(Player player, EnderPearl enderpearl) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Location blockloc = player.getLocation();
                Block block = blockloc.getBlock();
                Material mate = block.getType();
                if (mate != null) {
                    if (mate != Material.AIR) {
                        player.teleport(player.getLocation().add(0, 1, 0));
                        enderpearl.remove();
                        cancel();
                        return;
                    }
                }
                if (enderpearl.isDead()) {
                    cancel();
                    return;
                }
                if (enderpearl.getPassenger() == null) {
                    enderpearl.remove();
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 0L);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (Config.items_ender_pearl_chair_enabled && e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL && e.getDamage() == 5.0 && (e.getEntity().getLocation().getY() - e.getEntity().getLocation().getBlock().getLocation().getY()) != 0) {
            Player player = (Player) e.getEntity();
            if (BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player) != null) {
                e.setCancelled(true);
            }
        }
    }
}
