package net.wtako.WTAKOUtils.Commands;

import net.wtako.WTAKOUtils.Commands.fwtako.ArgHelp;
import net.wtako.WTAKOUtils.Commands.fwtako.ArgOff;
import net.wtako.WTAKOUtils.Commands.fwtako.ArgReload;
import net.wtako.WTAKOUtils.Commands.fwtako.ArgTSh;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandWut implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                new ArgHelp(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                new ArgReload(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("tsh")) {
                new ArgTSh(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("off")) {
                new ArgOff(sender);
                return true;
            }
        }
        return false;
    }
}
