package net.wtako.WTAKODeath.Commands.Wdt;

import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgClear {

    public ArgClear(CommandSender sender) {
        if (!sender.hasPermission(Main.getInstance().getProperty("artifactId") + ".admin")) {
            sender.sendMessage(Lang.NO_PERMISSION_COMMAND.toString());
            return;
        }
        DeathGuard.killAllDeathGuards();
        sender.sendMessage(Lang.ALL_GUARDS_KILLED.toString());
    }

}
