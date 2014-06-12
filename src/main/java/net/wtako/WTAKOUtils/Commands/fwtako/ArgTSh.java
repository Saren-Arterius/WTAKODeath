package net.wtako.WTAKOUtils.Commands.fwtako;

import net.wtako.WTAKOUtils.Utils.Lang;
import net.wtako.WTAKOUtils.Utils.ScoreboardUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgTSh {

    public ArgTSh(CommandSender sender) {
        if (ScoreboardUtils.noShowScoreboardPlayers.contains(((Player) sender).getUniqueId())) {
            ScoreboardUtils.noShowScoreboardPlayers.remove(((Player) sender).getUniqueId());
            sender.sendMessage(Lang.DEATH_INFO_SHOW_AGAIN.toString());
        } else {
            ScoreboardUtils.noShowScoreboardPlayers.add(((Player) sender).getUniqueId());
            sender.sendMessage(Lang.DEATH_INFO_NO_LONGER_SHOW.toString());
        }
    }

}
