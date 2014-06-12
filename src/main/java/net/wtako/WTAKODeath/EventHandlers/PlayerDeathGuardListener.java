package net.wtako.WTAKODeath.EventHandlers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.ExperienceManager;
import net.wtako.WTAKODeath.Utils.ItemStackUtils;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDeathGuardListener implements Listener {

    private static HashMap<UUID, ArrayList<ItemStack>> returnItemsOnRespawn = new HashMap<UUID, ArrayList<ItemStack>>();

    @EventHandler(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(final PlayerDeathEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("InventoryProtection.Enable")) {
            return;
        }
        if (!event.getEntity().hasPermission(
                Main.getInstance().getProperty("artifactId") + ".canHaveDeathItemProtection")) {
            return;
        }
        if (event.getEntity().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final ArrayList<ArrayList<ItemStack>> keepAndDrop = ItemStackUtils.getSampleOfItemStack(event.getDrops(), Main
                .getInstance().getConfig().getInt("InventoryProtection.ItemRetainPercentage"));
        PlayerDeathGuardListener.returnItemsOnRespawn.put(event.getEntity().getUniqueId(), keepAndDrop.get(0));
        final int keepExpPercentage = PlayerDeathGuardListener.getPercentage(Main.getInstance().getConfig()
                .getInt("InventoryProtection.ExpRetainPercentage"));
        final int deleteExpPercentage = PlayerDeathGuardListener.getPercentage(Main.getInstance().getConfig()
                .getInt("InventoryProtection.ExpDeletePercentage"));
        final int guardExpPercentage = PlayerDeathGuardListener.getPercentage(100 - keepExpPercentage
                - deleteExpPercentage);
        final ExperienceManager manager = new ExperienceManager(event.getEntity());

        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Enable")
                && event.getEntity().hasPermission(Main.getInstance().getProperty("artifactId") + ".canHaveGuard")) {
            final DeathGuard deathGuard = new DeathGuard(event.getEntity(), keepAndDrop.get(1), Math.round(manager
                    .getCurrentExp() * (guardExpPercentage / 100F)));
            DeathGuard.getAllDeathGuards().add(deathGuard);
            event.getEntity().sendMessage(MessageFormat.format(Lang.GUARD_SPAWN.toString(), deathGuard.toString()));
            event.getEntity().sendMessage(Lang.HELP_GUARDS.toString());
            manager.setExp(manager.getCurrentExp() * (keepExpPercentage / 100F));
            event.getDrops().clear();
        } else {
            event.getDrops().clear();
            for (final ItemStack itemStack: keepAndDrop.get(1)) {
                event.getDrops().add(itemStack);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public static void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (!event.getPlayer().hasPermission(
                Main.getInstance().getProperty("artifactId") + ".canHaveDeathItemProtection")
                || !PlayerDeathGuardListener.returnItemsOnRespawn.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        for (final ItemStack itemStack: PlayerDeathGuardListener.returnItemsOnRespawn.remove(event.getPlayer()
                .getUniqueId())) {
            ItemStackUtils.giveToPlayerOrDrop(itemStack, event.getPlayer(), event.getRespawnLocation());
        }
    }

    public static int getPercentage(int number) {
        number = number > 100 ? 100 : number;
        number = number < 0 ? 0 : number;
        return number;
    }

}
