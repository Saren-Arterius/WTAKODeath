package net.wtako.WTAKODeath.Events;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerDeathProtectEvent extends Event implements Cancellable {

    private static final HandlerList   handlers = new HandlerList();
    private boolean                    cancelled;
    private final Player               player;
    private final ArrayList<ItemStack> keepItems;
    private final ArrayList<ItemStack> dropItems;
    private final float                expBeforeDeath;
    private float                      expKept;
    private float                      expGuard;
    private double                     guardMaxHealth;
    private boolean                    useDeathGuard;

    public PlayerDeathProtectEvent(Player player, ArrayList<ItemStack> keepItems, ArrayList<ItemStack> dropItems,
            float expBeforeDeath, float expKept, float expGuard, double guardMaxHealth) {
        this.player = player;
        this.keepItems = keepItems;
        this.dropItems = dropItems;
        this.expBeforeDeath = expBeforeDeath;
        this.expKept = expKept;
        this.expGuard = expGuard;
        this.guardMaxHealth = guardMaxHealth;
        useDeathGuard = true;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<ItemStack> getKeepItems() {
        return keepItems;
    }

    public ArrayList<ItemStack> getDropItems() {
        return dropItems;
    }

    public float getExpBeforeDeath() {
        return expBeforeDeath;
    }

    public float getExpKept() {
        return expKept;
    }

    public void setExpKept(float expKept) {
        this.expKept = expKept;
    }

    public float getExpGuarded() {
        return expGuard;
    }

    public void setExpGuard(float expGuard) {
        this.expGuard = expGuard;
    }

    public double getGuardMaxHealth() {
        return guardMaxHealth;
    }

    public void setGuardMaxHealth(double guardMaxHealth) {
        this.guardMaxHealth = guardMaxHealth;
    }

    public boolean useDeathGuard() {
        return useDeathGuard;
    }

    public void setUseDeathGuard(boolean useDeathGuard) {
        this.useDeathGuard = useDeathGuard;
    }

    @Override
    public boolean isCancelled() {
        // TODO Auto-generated method stub
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return PlayerDeathProtectEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PlayerDeathProtectEvent.handlers;
    }

}
