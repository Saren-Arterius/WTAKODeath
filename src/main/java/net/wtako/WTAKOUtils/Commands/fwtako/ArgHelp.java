package net.wtako.WTAKOUtils.Commands.fwtako;

import net.wtako.WTAKOUtils.Main;
import net.wtako.WTAKOUtils.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgHelp {

    public ArgHelp(CommandSender sender) {
        sender.sendMessage(Main.getInstance().getName() + " v" + Main.getInstance().getProperty("version"));
        sender.sendMessage("Author: " + Main.getInstance().getProperty("author"));
        sender.sendMessage(Lang.HELP_OFF.toString());
        sender.sendMessage(Lang.HELP_TSH.toString());
        sender.sendMessage(Lang.HELP_RELOAD.toString());
    }

}
