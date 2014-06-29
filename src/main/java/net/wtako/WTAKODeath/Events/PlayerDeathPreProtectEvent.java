package net.wtako.WTAKODeath.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDeathPreProtectEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player             player;
    private int                      itemRetainPercentage;
    private int                      keepExpPercentage;
    private int                      deleteExpPercentage;

    public PlayerDeathPreProtectEvent(Player player, int itemRetainPercentage, int keepExpPercentage,
            int deleteExpPercentage) {
        this.player = player;
        this.itemRetainPercentage = itemRetainPercentage;
        this.keepExpPercentage = keepExpPercentage;
        this.deleteExpPercentage = deleteExpPercentage;
    }

    public Player getPlayer() {
        return player;
    }

    public int getItemRetainPercentage() {
        return itemRetainPercentage;
    }

    public void setItemRetainPercentage(int itemRetainPercentage) {
        this.itemRetainPercentage = itemRetainPercentage;
    }

    public int getKeepExpPercentage() {
        return keepExpPercentage;
    }

    public void setKeepExpPercentage(int keepExpPercentage) {
        this.keepExpPercentage = keepExpPercentage;
    }

    public int getDeleteExpPercentage() {
        return deleteExpPercentage;
    }

    public void setDeleteExpPercentage(int deleteExpPercentage) {
        this.deleteExpPercentage = deleteExpPercentage;
    }

    @Override
    public HandlerList getHandlers() {
        return PlayerDeathPreProtectEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PlayerDeathPreProtectEvent.handlers;
    }
}
