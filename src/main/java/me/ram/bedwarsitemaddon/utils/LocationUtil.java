package me.ram.bedwarsitemaddon.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class LocationUtil {
    public static Location getLocation(Location location, int x, int y, int z) {
        Location loc = location.getBlock().getLocation();
        loc.add(x, y, z);
        return loc;
    }

    public static Location getLocationYaw(Location location, double X, double Y, double Z) {
        Location loc = location.clone();
        double radians = Math.toRadians(location.getYaw());
        double x = Math.cos(radians) * X;
        double z = Math.sin(radians) * X;
        loc.add(x, Y, z);
        loc.setPitch(0);
        return loc;
    }

    public static Vector getPosition(Location location1, Location location2) {
        double X = location1.getX() - location2.getX();
        double Y = location1.getY() - location2.getY();
        double Z = location1.getZ() - location2.getZ();
        return new Vector(X, Y, Z);
    }

    public static Vector getPosition(Location location1, Location location2, double Y) {
        double X = location1.getX() - location2.getX();
        double Z = location1.getZ() - location2.getZ();
        return new Vector(X, Y, Z);
    }

    public static List<Player> getLocationPlayers(Location location) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() == location.getWorld() && (int) location.getX() == (int) player.getLocation().getX() && (int) location.getY() == (int) player.getLocation().getY() && (int) location.getZ() == (int) player.getLocation().getZ()) {
                players.add(player);
            }
        }
        return players;
    }
}
