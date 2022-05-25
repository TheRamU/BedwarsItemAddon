package me.ram.bedwarsitemaddon.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.BlockIterator;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import me.ram.bedwarsitemaddon.config.Config;

public class ExplosionProof implements Listener {

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (!Config.items_explosion_proof_enabled) {
            return;
        }
        Location location = e.getEntity().getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
        Game game = null;
        for (Game g : BedwarsRel.getInstance().getGameManager().getGames()) {
            if (g.getState() == GameState.RUNNING && g.getRegion().isInRegion(location)) {
                game = g;
            }
        }
        if (game == null) {
            return;
        }
        List<Block> block_list = new ArrayList<>();
        for (Block block : e.blockList()) {
            if (block.getType().equals(Material.GLASS) || block.getType().equals(Material.STAINED_GLASS) || !game.getRegion().isPlacedBlock(block)) {
                continue;
            }
            Location loc = block.getLocation().add(0.5, 0.5, 0.5);
            boolean isadd = true;
            Iterator<Block> itr = new BlockIterator(location.getWorld(), location.toVector(), loc.clone().subtract(location).toVector(), 0, (int) Math.ceil(loc.distance(location)));
            while (itr.hasNext()) {
                Block b = itr.next();
                if (b.getType().equals(Material.GLASS) || b.getType().equals(Material.STAINED_GLASS)) {
                    isadd = false;
                    break;
                }
                if (b.equals(block)) {
                    break;
                }
            }
            if (isadd) {
                block_list.add(block);
            }
        }
        e.blockList().clear();
        e.blockList().addAll(block_list);
    }
}
