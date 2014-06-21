package net.wtako.WTAKODeath.Commands.Wdt;

import java.util.LinkedList;

import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgDie {

    public ArgDie(CommandSender sender) {
        int counter = 0;
        final LinkedList<DeathGuard> deathGuardsToKill = new LinkedList<DeathGuard>();
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (deathGuard.getOwner() == (Player) sender) {
                counter++;
                deathGuardsToKill.add(deathGuard);
            }
        }
        if (counter == 0) {
            sender.sendMessage(Lang.YOU_DONT_HAVE_GUARDS.toString());
        } else {
            for (final DeathGuard deathGuard: deathGuardsToKill) {
                deathGuard.destroy();
            }
            deathGuardsToKill.clear();
        }
    }

}
