package net.wtako.WTAKODeath.Commands.Wdt;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import net.milkbowl.vault.economy.Economy;
import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.ExperienceManager;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ArgBless implements Listener {

    public static HashMap<UUID, LinkedList<DeathGuard>> inCoversation = new HashMap<UUID, LinkedList<DeathGuard>>();

    public ArgBless(CommandSender sender) {
        if (Main.getInstance().getConfig().getInt("InventoryProtection.DeathGuardSystem.BlessGiveBackCost") == -1) {
            sender.sendMessage(Lang.NO_PERMISSION_COMMAND.toString());
            return;
        }

        final UUID uuid = ((Player) sender).getUniqueId();

        if (ArgBless.inCoversation.containsKey(uuid)) {
            ArgBless.fail(uuid);
        }

        int counter = 0;
        final LinkedList<DeathGuard> deathGuardsToBless = new LinkedList<DeathGuard>();
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (deathGuard.getOwner() == (Player) sender) {
                counter++;
                deathGuardsToBless.add(deathGuard);
            }
        }
        if (counter == 0) {
            sender.sendMessage(Lang.YOU_DONT_HAVE_GUARDS.toString());
        } else {
            ArgBless.inCoversation.put(uuid, deathGuardsToBless);
            if (Main.getInstance().getConfig().getInt("InventoryProtection.DeathGuardSystem.BlessGiveBackCost") == -2) {
                final String itemRequired = Main.getInstance().getConfig()
                        .getString("InventoryProtection.DeathGuardSystem.BlessGiveBackItemCostMetaName");
                final int itemRequiredAmount = Main.getInstance().getConfig()
                        .getInt("InventoryProtection.DeathGuardSystem.BlessGiveBackItemCostAmount");
                sender.sendMessage(MessageFormat.format(Lang.GUARD_PAY_ITEM_TO_GIVE_BACK.toString(),
                        itemRequiredAmount, itemRequired));
            } else {
                final double moneyRequired = Main.getInstance().getConfig()
                        .getDouble("InventoryProtection.DeathGuardSystem.BlessGiveBackCost");
                sender.sendMessage(MessageFormat.format(Lang.GUARD_PAY_TO_GIVE_BACK.toString(), moneyRequired));
            }
        }
    }

    public static void fail(UUID uuid) {
        Main.getInstance().getServer().getPlayer(uuid).sendMessage(Lang.GUARD_PAY_TO_GIVE_BACK_CANCELLED.toString());
        ArgBless.inCoversation.remove(uuid);
    }

    public static void proceed(UUID uuid) {
        final Player player = Main.getInstance().getServer().getPlayer(uuid);
        int guardsBlessed = 0;
        final double moneyRequired = Main.getInstance().getConfig()
                .getDouble("InventoryProtection.DeathGuardSystem.BlessGiveBackCost");
        final String itemNameRequired = Main.getInstance().getConfig()
                .getString("InventoryProtection.DeathGuardSystem.BlessGiveBackItemCostMetaName");
        final Material itemTypeRequired = Material.valueOf(Main.getInstance().getConfig()
                .getString("InventoryProtection.DeathGuardSystem.BlessGiveBackItemCostType").toUpperCase());
        final int itemAmountRequired = Main.getInstance().getConfig()
                .getInt("InventoryProtection.DeathGuardSystem.BlessGiveBackItemCostAmount");
        if (Main.getInstance().getConfig().getInt("InventoryProtection.DeathGuardSystem.BlessGiveBackCost") == -2) {
            for (final DeathGuard deathGuard: ArgBless.inCoversation.remove(uuid)) {
                if (ArgBless.removeItem(player.getInventory(), itemTypeRequired, itemNameRequired, itemAmountRequired)) {
                    deathGuard.giveBack();
                    guardsBlessed++;
                } else {
                    player.sendMessage(MessageFormat.format(Lang.GUARD_PAY_ITEM_TO_GIVE_BACK_NOT_ENOUGH.toString(),
                            itemNameRequired));
                    break;
                }
            }
        } else if (Main.getInstance().getConfig().getBoolean("System.VaultSupport")) {
            try {
                final RegisteredServiceProvider<Economy> provider = Main.getInstance().getServer().getServicesManager()
                        .getRegistration(net.milkbowl.vault.economy.Economy.class);
                final Economy economy = provider.getProvider();
                for (final DeathGuard deathGuard: ArgBless.inCoversation.remove(uuid)) {
                    if (!deathGuard.isValid()) {
                        continue;
                    }
                    if (economy.has(player.getName(), moneyRequired)) {
                        economy.withdrawPlayer(player.getName(), moneyRequired);
                        deathGuard.giveBack();
                        guardsBlessed++;
                    } else {
                        player.sendMessage(Lang.GUARD_PAY_TO_GIVE_BACK_NOT_ENOUGH_MONEY.toString());
                        break;
                    }
                }
            } catch (final Error e) {
                player.sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Vault"));
                return;
            }
        } else {
            final ExperienceManager man = new ExperienceManager(player);
            for (final DeathGuard deathGuard: ArgBless.inCoversation.remove(uuid)) {
                if (!deathGuard.isValid()) {
                    continue;
                }
                if (man.hasExp(moneyRequired)) {
                    man.changeExp(-moneyRequired);
                    deathGuard.giveBack();
                    guardsBlessed++;
                } else {
                    player.sendMessage(Lang.GUARD_PAY_TO_GIVE_BACK_NOT_ENOUGH_MONEY.toString());
                    break;
                }
            }
        }
        if (guardsBlessed > 0) {
            if (Main.getInstance().getConfig().getInt("InventoryProtection.DeathGuardSystem.BlessGiveBackCost") == -2) {
                player.sendMessage(MessageFormat.format(Lang.GUARD_PAY_ITEM_TO_GIVE_BACK_SUCCEED.toString(),
                        guardsBlessed, guardsBlessed * itemAmountRequired, itemNameRequired));
            } else {
                player.sendMessage(MessageFormat.format(Lang.GUARD_PAY_TO_GIVE_BACK_SUCCEED.toString(), guardsBlessed,
                        guardsBlessed * moneyRequired));
            }
        }
    }

    public static boolean removeItem(Inventory inventory, Material itemType, String displayName, int amount) {
        int currentAmount = 0;
        for (final ItemStack item: inventory) {
            if (item == null) {
                continue;
            }
            if (item.getType() == itemType && item.hasItemMeta()
                    && item.getItemMeta().getDisplayName().equalsIgnoreCase(displayName)) {
                currentAmount += item.getAmount();
            }
        }
        if (currentAmount < amount) {
            return false;
        }
        for (final ItemStack item: inventory) {
            if (item == null) {
                continue;
            }
            if (item.getType() == itemType && item.hasItemMeta()
                    && item.getItemMeta().getDisplayName().equalsIgnoreCase(displayName)) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                    return true;
                } else if (item.getAmount() == amount) {
                    inventory.remove(item);
                    return true;
                } else {
                    amount -= item.getAmount();
                    inventory.remove(item);
                }
            }
        }
        return true;
    }

}
