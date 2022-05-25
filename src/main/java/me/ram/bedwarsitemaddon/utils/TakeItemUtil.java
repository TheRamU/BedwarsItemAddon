package me.ram.bedwarsitemaddon.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bedwarsrel.BedwarsRel;

public class TakeItemUtil {
    public static void TakeItem(Player player, ItemStack stack) {
        if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
            if (player.getInventory().getItemInHand() != null) {
                ItemStack itemInHand = player.getInventory().getItemInHand();
                if (itemInHand.getType() == stack.getType()) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                    player.getInventory().setItemInHand(itemInHand);
                    return;
                }
            }
        } else {
            if (player.getInventory().getItemInMainHand() != null) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType() == stack.getType()) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                    player.getInventory().setItemInMainHand(itemInHand);
                    return;
                }
            }
            if (player.getInventory().getItemInOffHand() != null) {
                ItemStack itemInHand = player.getInventory().getItemInOffHand();
                if (itemInHand.getType() == stack.getType()) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                    player.getInventory().setItemInOffHand(itemInHand);
                    return;
                }
            }
        }
        ItemMeta meta = stack.getItemMeta();
        ItemStack itemStack = new ItemStack(stack.getType(), 1);
        itemStack.setItemMeta(meta);
        player.getInventory().removeItem(itemStack);
    }
}
