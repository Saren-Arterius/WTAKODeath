package net.wtako.WTAKODeath.EventHandlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.ExperienceManager;
import net.wtako.WTAKODeath.Utils.ItemStackUtils;
import net.wtako.WTAKODeath.Utils.Lang;
import net.wtako.WTAKODeath.Utils.StringUtils;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class PlayerDeathGuardListener implements Listener {

    private static HashMap<UUID, ArrayList<ItemStack>> returnItemsOnRespawn = new HashMap<UUID, ArrayList<ItemStack>>();
    private static HashMap<UUID, Long>                 deathThrottle        = new HashMap<UUID, Long>();

    @EventHandler(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(final PlayerDeathEvent event) {
        if (event.getEntity().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        boolean worldGuardAllows = true;
        if (PlayerDeathGuardListener.deathThrottle.containsKey(event.getEntity().getUniqueId())
                && PlayerDeathGuardListener.deathThrottle.get(event.getEntity().getUniqueId()) > System
                        .currentTimeMillis()) {
            worldGuardAllows = false;
        } else {
            PlayerDeathGuardListener.deathThrottle.put(event.getEntity().getUniqueId(), System.currentTimeMillis()
                    + Main.getInstance().getConfig().getLong("InventoryProtection.ThrottleSeconds") * 1000);
        }
        if (Main.getInstance().getConfig().getBoolean("System.WorldGuardSupport")) {
            try {
                final WorldGuardPlugin worldGuard = PlayerDeathGuardListener.getWorldGuard();
                final RegionManager regionManager = worldGuard.getRegionManager(event.getEntity().getWorld());
                final ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitUtil.toVector(event
                        .getEntity().getLocation()));
                if (!set.allows(DefaultFlag.MUSHROOMS)) {
                    worldGuardAllows = false;
                }
            } catch (final Error e) {
                event.getEntity().sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "WorldGuard"));
                e.printStackTrace();
                worldGuardAllows = false;
            }
        }
        final ExperienceManager manager = new ExperienceManager(event.getEntity());
        if (!event.getEntity().hasPermission(
                Main.getInstance().getProperty("artifactId") + ".canHaveDeathItemProtection")
                || !worldGuardAllows) {
            if (Main.getInstance().getConfig().getBoolean("InventoryProtection.EnableItemLog")) {
                Main.getInstance().getServer().getScheduler()
                        .runTaskLaterAsynchronously(Main.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final FileWriter writer = new FileWriter(new File(Main.getInstance()
                                            .getDataFolder(), "log.log"), true);
                                    final Date currentDate = new Date(System.currentTimeMillis());
                                    for (final ItemStack itemStack: event.getDrops()) {
                                        writer.append(MessageFormat.format(Lang.LOG_FORMAT_ITEM_DROPPED.toString()
                                                + "\r\n", currentDate, event.getEntity().getName(),
                                                itemStack.toString(),
                                                StringUtils.locationToString(event.getEntity().getLocation())));
                                    }
                                    writer.close();
                                } catch (final IOException e) {
                                    event.getEntity().sendMessage(
                                            MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                                    e.printStackTrace();
                                }
                            }
                        }, 10L);
            }
            if (Main.getInstance().getConfig().getBoolean("InventoryProtection.EnableEXPLog")) {
                Main.getInstance().getServer().getScheduler()
                        .runTaskLaterAsynchronously(Main.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final FileWriter writer = new FileWriter(new File(Main.getInstance()
                                            .getDataFolder(), "log.log"), true);
                                    writer.append(MessageFormat.format(Lang.LOG_FORMAT_EXP_KEPT_GUARDED.toString()
                                            + "\r\n", new Date(System.currentTimeMillis()),
                                            event.getEntity().getName(), 0, 0, manager.getCurrentExp()));
                                    writer.close();
                                } catch (final IOException e) {
                                    event.getEntity().sendMessage(
                                            MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                                    e.printStackTrace();
                                }
                            }
                        }, 10L);
            }
            return;
        }

        if (!Main.getInstance().getConfig().getBoolean("InventoryProtection.Enable")) {
            return;
        }

        final ArrayList<ArrayList<ItemStack>> keepAndDrop = ItemStackUtils.getSampleOfItemStack(event.getDrops(), Main
                .getInstance().getConfig().getInt("InventoryProtection.ItemRetainPercentage"));

        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.EnableItemLog")) {
            Main.getInstance().getServer().getScheduler()
                    .runTaskLaterAsynchronously(Main.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final FileWriter writer = new FileWriter(new File(Main.getInstance().getDataFolder(),
                                        "log.log"), true);
                                final Date currentDate = new Date(System.currentTimeMillis());
                                for (final ItemStack itemStack: keepAndDrop.get(0)) {
                                    writer.append(MessageFormat.format(Lang.LOG_FORMAT_ITEM_KEPT.toString() + "\r\n",
                                            currentDate, event.getEntity().getName(), itemStack.toString()));
                                }
                                for (final ItemStack itemStack: keepAndDrop.get(1)) {
                                    writer.append(MessageFormat.format(
                                            Lang.LOG_FORMAT_ITEM_DROPPED.toString() + "\r\n", currentDate, event
                                                    .getEntity().getName(), itemStack.toString(), StringUtils
                                                    .locationToString(event.getEntity().getLocation())));
                                }
                                writer.close();
                            } catch (final IOException e) {
                                event.getEntity().sendMessage(
                                        MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                                e.printStackTrace();
                            }
                        }
                    }, 10L);
        }

        PlayerDeathGuardListener.returnItemsOnRespawn.put(event.getEntity().getUniqueId(), keepAndDrop.get(0));
        final int keepExpPercentage = PlayerDeathGuardListener.getPercentage(Main.getInstance().getConfig()
                .getInt("InventoryProtection.ExpRetainPercentage"));
        final int deleteExpPercentage = PlayerDeathGuardListener.getPercentage(Main.getInstance().getConfig()
                .getInt("InventoryProtection.DeathGuardSystem.ExpDeletePercentage"));
        final int guardExpPercentage = PlayerDeathGuardListener.getPercentage(100 - keepExpPercentage
                - deleteExpPercentage);

        final float expBeforeDeath = manager.getCurrentExp() * (keepExpPercentage / 100F);
        final float expKept = expBeforeDeath * (keepExpPercentage / 100F);
        final float expGuard = expBeforeDeath * (guardExpPercentage / 100F);

        Main.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                event.getEntity().sendMessage(
                        MessageFormat.format(Lang.YOU_KEPT_ITEMS_LEVELS.toString(), keepAndDrop.get(0).size(),
                                manager.getLevelForExp(Math.round(expKept))));
            }
        }, 8L);

        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.EnableEXPLog")) {
            Main.getInstance().getServer().getScheduler()
                    .runTaskLaterAsynchronously(Main.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final FileWriter writer = new FileWriter(new File(Main.getInstance().getDataFolder(),
                                        "log.log"), true);
                                writer.append(MessageFormat.format(
                                        Lang.LOG_FORMAT_EXP_KEPT_GUARDED.toString() + "\r\n",
                                        new Date(System.currentTimeMillis()), event.getEntity().getName(), expKept,
                                        expGuard, expBeforeDeath));
                                writer.close();
                            } catch (final IOException e) {
                                event.getEntity().sendMessage(
                                        MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                                e.printStackTrace();
                            }
                        }
                    }, 10L);
        }

        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Enable")
                && event.getEntity().hasPermission(Main.getInstance().getProperty("artifactId") + ".canHaveGuard")) {
            final DeathGuard deathGuard = new DeathGuard(event.getEntity(), keepAndDrop.get(1), Math.round(expGuard));
            DeathGuard.getAllDeathGuards().add(deathGuard);
            Main.getInstance().getServer().getScheduler()
                    .runTaskLaterAsynchronously(Main.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            event.getEntity().sendMessage(
                                    MessageFormat.format(Lang.GUARD_SPAWN.toString(), deathGuard.toString()));
                            event.getEntity().sendMessage(Lang.HELP_GUARDS.toString());
                        }
                    }, 10L);
            manager.setExp(expKept);
            event.getEntity().getInventory().clear();
            event.getDrops().clear();
        } else {
            manager.setExp(expKept);
            event.getEntity().getInventory().clear();
            for (final ItemStack itemStack: keepAndDrop.get(1)) {
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), itemStack);
            }
            event.getDrops().clear();
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

    private static WorldGuardPlugin getWorldGuard() {
        final Plugin plugin = Main.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    public static int getPercentage(int number) {
        number = number > 100 ? 100 : number;
        number = number < 0 ? 0 : number;
        return number;
    }

}
