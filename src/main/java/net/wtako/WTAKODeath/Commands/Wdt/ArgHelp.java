package net.wtako.WTAKODeath.Commands.Wdt;

import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgHelp {

    public ArgHelp(CommandSender sender) {
        sender.sendMessage(Main.getInstance().getName() + " v" + Main.getInstance().getProperty("version"));
        sender.sendMessage("Author: " + Main.getInstance().getProperty("author"));
        sender.sendMessage(Lang.HELP_OFF.toString());
        sender.sendMessage(Lang.HELP_TSH.toString());
        sender.sendMessage(Lang.HELP_GUARDS.toString());
        sender.sendMessage(Lang.HELP_DIE.toString());
        sender.sendMessage(Lang.HELP_BLESS.toString());
        sender.sendMessage(Lang.HELP_ALL_GUARDS.toString());
        sender.sendMessage(Lang.HELP_CLEAR.toString());
        sender.sendMessage(Lang.HELP_RELOAD.toString());
    }

}
