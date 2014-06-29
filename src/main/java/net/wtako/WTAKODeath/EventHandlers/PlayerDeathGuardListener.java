package net.wtako.WTAKODeath.EventHandlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Events.PlayerDeathPreProtectEvent;
import net.wtako.WTAKODeath.Events.PlayerDeathProtectEvent;
import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.ExperienceManager;
import net.wtako.WTAKODeath.Utils.ItemStackUtils;
import net.wtako.WTAKODeath.Utils.Lang;
import net.wtako.WTAKODeath.Utils.StringUtils;

import org.bukkit.GameMode;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class PlayerDeathGuardListener implements Listener {

    private static HashMap<UUID, ArrayList<ItemStack>> returnItemsOnRespawn = new HashMap<UUID, ArrayList<ItemStack>>();
    private static HashMap<UUID, Long>                 deathThrottle        = new HashMap<UUID, Long>();

    @EventHandler
    public static void onPlayerDeath(final PlayerDeathEvent event) {
        if (event.getEntity().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (event.getEntity().getWorld().getGameRuleValue("keepInventory").toLowerCase().startsWith("t")) {
            return;
        }
        boolean worldGuardAllows = Main.getInstance().getConfig().getBoolean("InventoryProtection.Enable");
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
            PlayerDeathGuardListener.logItemsToFile(event.getEntity(), event.getDrops(), false, 8L);
            if (Main.getInstance().getConfig().getBoolean("InventoryProtection.EnableEXPLog")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            final FileWriter writer = new FileWriter(new File(Main.getInstance().getDataFolder(),
                                    "log.log"), true);
                            writer.append(MessageFormat.format(Lang.LOG_FORMAT_EXP_KEPT_GUARDED.toString() + "\r\n",
                                    new Date(System.currentTimeMillis()), event.getEntity().getName(), 0, 0,
                                    manager.getCurrentExp()));
                            writer.close();
                        } catch (final IOException e) {
                            event.getEntity()
                                    .sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                            e.printStackTrace();
                        }
                    }
                }.runTaskLaterAsynchronously(Main.getInstance(), 2L);
            }
            return;
        }

        final PlayerDeathPreProtectEvent playerDeathPreProtectEvent = new PlayerDeathPreProtectEvent(event.getEntity(),
                PlayerDeathGuardListener.getPercentage(Main.getInstance().getConfig()
                        .getInt("InventoryProtection.ItemRetainPercentage")),
                PlayerDeathGuardListener.getPercentage(Main.getInstance().getConfig()
                        .getInt("InventoryProtection.ExpRetainPercentage")),
                PlayerDeathGuardListener.getPercentage(Main.getInstance().getConfig()
                        .getInt("InventoryProtection.DeathGuardSystem.ExpDeletePercentage")));
        Main.getInstance().getServer().getPluginManager().callEvent(playerDeathPreProtectEvent);

        final ArrayList<ArrayList<ItemStack>> keepAndDrop = ItemStackUtils.getSampleOfItemStack(event.getDrops(),
                playerDeathPreProtectEvent.getItemRetainPercentage());
        final double guardMaxHealth = Main.getInstance().getConfig()
                .getDouble("InventoryProtection.DeathGuardSystem.ProtectSeconds");
        final int guardExpPercentage = PlayerDeathGuardListener.getPercentage(100
                - playerDeathPreProtectEvent.getKeepExpPercentage()
                - playerDeathPreProtectEvent.getDeleteExpPercentage());
        final float expKept = manager.getCurrentExp() * (playerDeathPreProtectEvent.getKeepExpPercentage() / 100F);
        final float expGuard = manager.getCurrentExp() * (guardExpPercentage / 100F);
        final PlayerDeathProtectEvent playerDeathProtectEvent = new PlayerDeathProtectEvent(event.getEntity(),
                keepAndDrop.get(0), keepAndDrop.get(1), manager.getCurrentExp(), expKept, expGuard, guardMaxHealth);
        Main.getInstance().getServer().getPluginManager().callEvent(playerDeathProtectEvent);

        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.EnableEXPLog")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        final FileWriter writer = new FileWriter(
                                new File(Main.getInstance().getDataFolder(), "log.log"), true);
                        writer.append(MessageFormat.format(Lang.LOG_FORMAT_EXP_KEPT_GUARDED.toString() + "\r\n",
                                new Date(System.currentTimeMillis()), event.getEntity().getName(),
                                playerDeathProtectEvent.getExpKept(), playerDeathProtectEvent.getExpGuarded(),
                                playerDeathProtectEvent.getExpBeforeDeath()));
                        writer.close();
                    } catch (final IOException e) {
                        event.getEntity().sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                        e.printStackTrace();
                    }
                }
            }.runTaskLaterAsynchronously(Main.getInstance(), 2L);
        }

        if (playerDeathProtectEvent.isCancelled()) {
            PlayerDeathGuardListener.logItemsToFile(event.getEntity(), event.getDrops(), false, 10L);
            return;
        }

        event.getDrops().clear();
        manager.setExp(playerDeathProtectEvent.getExpKept());
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Enable")
                && event.getEntity().hasPermission(Main.getInstance().getProperty("artifactId") + ".canHaveGuard")
                && playerDeathProtectEvent.useDeathGuard()) {
            final DeathGuard deathGuard = new DeathGuard(event.getEntity(), playerDeathProtectEvent.getDropItems(),
                    Math.round(playerDeathProtectEvent.getExpGuarded()), playerDeathProtectEvent.getGuardMaxHealth());
            DeathGuard.getAllDeathGuards().add(deathGuard);
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getEntity().sendMessage(
                            MessageFormat.format(Lang.GUARD_SPAWN.toString(), deathGuard.toString()));
                    event.getEntity().sendMessage(Lang.HELP_GUARDS.toString());
                }
            }.runTaskLaterAsynchronously(Main.getInstance(), 10L);
            PlayerDeathGuardListener.logItemsToFile(event.getEntity(), playerDeathProtectEvent.getDropItems(), false,
                    8L);
        } else {
            event.getDrops().addAll(playerDeathProtectEvent.getDropItems());
            PlayerDeathGuardListener.logItemsToFile(event.getEntity(), event.getDrops(), false, 8L);
            event.getEntity().getWorld().spawn(event.getEntity().getLocation(), ExperienceOrb.class)
                    .setExperience(Math.round(playerDeathProtectEvent.getExpGuarded()));
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                event.getEntity().sendMessage(
                        MessageFormat.format(Lang.YOU_KEPT_ITEMS_LEVELS.toString(), playerDeathProtectEvent
                                .getKeepItems().size(), manager.getLevelForExp(Math.round(playerDeathProtectEvent
                                .getExpGuarded()))));
            }
        }.runTaskLaterAsynchronously(Main.getInstance(), 8L);
        PlayerDeathGuardListener.returnItemsOnRespawn.put(event.getEntity().getUniqueId(),
                playerDeathProtectEvent.getKeepItems());
        PlayerDeathGuardListener.logItemsToFile(event.getEntity(), playerDeathProtectEvent.getKeepItems(), true, 5L);
    }

    @EventHandler
    public static void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (!event.getPlayer().hasPermission(
                Main.getInstance().getProperty("artifactId") + ".canHaveDeathItemProtection")
                || !PlayerDeathGuardListener.returnItemsOnRespawn.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                event.getPlayer().getInventory().clear();
                for (final ItemStack itemStack: PlayerDeathGuardListener.returnItemsOnRespawn.remove(event.getPlayer()
                        .getUniqueId())) {
                    ItemStackUtils.giveToPlayerOrDrop(itemStack, event.getPlayer(), event.getRespawnLocation());
                }
            }
        }.runTaskLater(Main.getInstance(), 20L);

    }

    @EventHandler
    public static void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (PlayerDeathGuardListener.returnItemsOnRespawn.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
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

    public static void returnAllItemsNow() {
        for (final Entry<UUID, ArrayList<ItemStack>> entry: PlayerDeathGuardListener.returnItemsOnRespawn.entrySet()) {
            final Player player = Main.getInstance().getServer().getPlayer(entry.getKey());
            if (player != null) {
                for (final ItemStack itemStack: entry.getValue()) {
                    ItemStackUtils.giveToPlayerOrDrop(itemStack, player, player.getLocation());
                }
            }
        }
        PlayerDeathGuardListener.returnItemsOnRespawn.clear();
    }

    public static void logItemsToFile(final Player player, final List<ItemStack> items, final boolean isKeep, Long delay) {
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.EnableItemLog")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        final FileWriter writer = new FileWriter(
                                new File(Main.getInstance().getDataFolder(), "log.log"), true);
                        final Date currentDate = new Date(System.currentTimeMillis());
                        if (isKeep) {
                            for (final ItemStack itemStack: items) {
                                writer.append(MessageFormat.format(Lang.LOG_FORMAT_ITEM_KEPT.toString() + "\r\n",
                                        currentDate, player.getName(), itemStack.toString()));
                            }
                        } else {
                            for (final ItemStack itemStack: items) {
                                writer.append(MessageFormat.format(Lang.LOG_FORMAT_ITEM_DROPPED.toString() + "\r\n",
                                        currentDate, player.getName(), itemStack.toString(),
                                        StringUtils.locationToString(player.getLocation())));
                            }
                        }
                        writer.close();
                    } catch (final IOException e) {
                        player.sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                        e.printStackTrace();
                    }
                }
            }.runTaskLaterAsynchronously(Main.getInstance(), delay);
        }
    }

}
