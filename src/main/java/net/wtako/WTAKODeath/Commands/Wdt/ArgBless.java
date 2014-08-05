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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
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
            final double moneyRequired = Main.getInstance().getConfig()
                    .getDouble("InventoryProtection.DeathGuardSystem.BlessGiveBackCost");
            sender.sendMessage(MessageFormat.format(Lang.GUARD_PAY_TO_GIVE_BACK.toString(), moneyRequired));
        }
    }

    public static void fail(UUID uuid) {
        Main.getInstance().getServer().getPlayer(uuid).sendMessage(Lang.GUARD_PAY_TO_GIVE_BACK_CANCELLED.toString());
        ArgBless.inCoversation.remove(uuid);
    }

    public static void proceed(UUID uuid) {
        final Player player = Main.getInstance().getServer().getPlayer(uuid);
        final double moneyRequired = Main.getInstance().getConfig()
                .getDouble("InventoryProtection.DeathGuardSystem.BlessGiveBackCost");
        double guardsBlessed = 0;
        if (Main.getInstance().getConfig().getBoolean("System.VaultSupport")) {
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
        player.sendMessage(MessageFormat.format(Lang.GUARD_PAY_TO_GIVE_BACK_SUCCEED.toString(), guardsBlessed,
                guardsBlessed * moneyRequired));
    }

}
