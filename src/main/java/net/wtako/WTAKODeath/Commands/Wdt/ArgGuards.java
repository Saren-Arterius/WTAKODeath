package net.wtako.WTAKODeath.Commands.Wdt;

import java.text.MessageFormat;

import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgGuards {

    public ArgGuards(CommandSender sender) {
        int counter = 0;
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (deathGuard.getOwner() == (Player) sender) {
                counter++;
                sender.sendMessage(MessageFormat.format("{0}. {1}", counter, deathGuard.toString()));
            }
        }
        if (counter == 0) {
            sender.sendMessage(Lang.YOU_DONT_HAVE_GUARDS.toString());
        }
    }

}
