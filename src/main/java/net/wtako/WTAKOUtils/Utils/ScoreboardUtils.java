package net.wtako.WTAKOUtils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.wtako.WTAKOUtils.Main;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class ScoreboardUtils {

    public static ArrayList<UUID>           noShowScoreboardPlayers = new ArrayList<UUID>();
    public static HashMap<UUID, BukkitTask> scoreboardRemoveTimers  = new HashMap<UUID, BukkitTask>();

    @SuppressWarnings("deprecation")
    public static void showScoreboardMessage(String title, String message, String delimiter, final Player player,
            final Long showDelay, final Long showTime) {
        if (ScoreboardUtils.scoreboardRemoveTimers.containsKey(player.getUniqueId())) {
            ScoreboardUtils.scoreboardRemoveTimers.remove(player.getUniqueId()).cancel();
        }

        final ScoreboardManager manager = Main.getInstance().getServer().getScoreboardManager();
        final Scoreboard board = manager.getNewScoreboard();
        final Objective objective = board.registerNewObjective("test", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(title);

        final String[] messageArray = message.split("%lb%");
        int currentScore = messageArray.length;
        for (final String item: messageArray) {
            final Score score = objective.getScore(Main
                    .getInstance()
                    .getServer()
                    .getOfflinePlayer(
                            item.equalsIgnoreCase("") ? StringUtils.toInvisible(String.valueOf(currentScore)) : item
                                    .substring(0, item.length() > 16 ? 15 : item.length())));
            score.setScore(currentScore);
            currentScore--;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setScoreboard(board);
                final BukkitTask removeTimer = new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setScoreboard(manager.getNewScoreboard());
                        ScoreboardUtils.scoreboardRemoveTimers.remove(player.getUniqueId());
                    }
                }.runTaskLater(Main.getInstance(), showTime - showDelay);
                ScoreboardUtils.scoreboardRemoveTimers.put(player.getUniqueId(), removeTimer);
            }
        }.runTaskLater(Main.getInstance(), showDelay);
    }
}
