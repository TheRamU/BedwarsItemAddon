package me.ram.bedwarsitemaddon.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import io.github.bedwarsrel.game.Game;
import me.ram.bedwarsitemaddon.items.EnumItem;

public class BedwarsUseItemEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Game game;
    private final Player player;
    private final EnumItem itemtype;
    private final ItemStack consumeitem;
    private Boolean cancelled = false;

    public BedwarsUseItemEvent(Game game, Player player, EnumItem itemtype, ItemStack consumeitem) {
        this.game = game;
        this.player = player;
        this.itemtype = itemtype;
        this.consumeitem = consumeitem;
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public EnumItem getItemType() {
        return itemtype;
    }

    public ItemStack getConsumeItem() {
        return consumeitem;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
