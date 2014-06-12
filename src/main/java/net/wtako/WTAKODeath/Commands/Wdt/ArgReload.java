package net.wtako.WTAKODeath.Commands.Wdt;

import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgReload {

    public ArgReload(CommandSender sender) {
        if (!sender.hasPermission(Main.getInstance().getProperty("artifactId") + ".reload")) {
            sender.sendMessage(Lang.NO_PERMISSION_COMMAND.toString());
            return;
        }
        Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
        Main.getInstance().getServer().getPluginManager().enablePlugin(Main.getInstance());
        Main.getInstance().reloadConfig();
        sender.sendMessage(Lang.PLUGIN_RELOADED.toString());
    }

}
