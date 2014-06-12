package net.wtako.WTAKODeath.Commands;

import net.wtako.WTAKODeath.Commands.Wdt.ArgHelp;
import net.wtako.WTAKODeath.Commands.Wdt.ArgOff;
import net.wtako.WTAKODeath.Commands.Wdt.ArgReload;
import net.wtako.WTAKODeath.Commands.Wdt.ArgTSh;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandWdt implements CommandExecutor {

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
