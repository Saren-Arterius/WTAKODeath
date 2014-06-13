package net.wtako.WTAKODeath.Commands.Wdt;

import java.text.MessageFormat;

import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgAllGuards {

    public ArgAllGuards(CommandSender sender) {
        if (!sender.hasPermission(Main.getInstance().getProperty("artifactId") + ".admin")) {
            sender.sendMessage(Lang.NO_PERMISSION_COMMAND.toString());
            return;
        }
        int counter = 0;
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            counter++;
            sender.sendMessage(MessageFormat.format("{0}. {1}", counter, deathGuard.toString()));
        }
        if (counter == 0) {
            sender.sendMessage(Lang.NO_GUARDS.toString());
        }
    }

}
