package net.wtako.WTAKOUtils.Commands.fwtako;

import net.wtako.WTAKOUtils.Main;
import net.wtako.WTAKOUtils.Utils.Lang;
import net.wtako.WTAKOUtils.Utils.ScoreboardUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgOff {

    public ArgOff(CommandSender sender) {
        if (!ScoreboardUtils.scoreboardRemoveTimers.containsKey(((Player) sender).getUniqueId())) {
            sender.sendMessage(Lang.DEATH_INFO_NOT_DISPLAYING.toString());
            return;
        }
        ((Player) sender).setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
        ScoreboardUtils.scoreboardRemoveTimers.remove(((Player) sender).getUniqueId()).cancel();
        sender.sendMessage(Lang.DEATH_INFO_REMOVED.toString());
    }
}
