package me.ram.bedwarsitemaddon.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;

public class NMS {

    public static void teleportEntity(Game game, Entity entity, Location location) {
        if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
            try {
                Constructor<?> constructor = Utils.getNMSClass("PacketPlayOutEntityTeleport").getConstructor(int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class);
                Method method = Utils.getNMSClass("MathHelper").getMethod("floor", double.class);
                Object packet = constructor.newInstance(entity.getEntityId(), method.invoke(null, location.getX() * 32.0D), method.invoke(null, location.getY() * 32.0D), method.invoke(null, location.getZ() * 32.0D), (byte) (location.getYaw() * 256.0f / 360.0f), (byte) (location.getPitch() * 256.0f / 360.0f), true);
                for (Player player : game.getPlayers()) {
                    Utils.sendPacket(player, packet);
                }
            } catch (Exception ignored) {
            }
        } else {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
            packet.getIntegers().write(0, entity.getEntityId());
            packet.getDoubles().write(0, location.getX());
            packet.getDoubles().write(1, location.getY());
            packet.getDoubles().write(2, location.getZ());
            packet.getBytes().write(0, (byte) (location.getYaw() * 256.0f / 360.0f));
            packet.getBytes().write(1, (byte) (location.getPitch() * 256.0f / 360.0f));
            packet.getBooleans().write(0, true);
            try {
                for (Player player : game.getPlayers()) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
