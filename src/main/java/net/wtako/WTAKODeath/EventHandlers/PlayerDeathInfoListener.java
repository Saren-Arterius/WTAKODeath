package net.wtako.WTAKODeath.EventHandlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Utils.Lang;
import net.wtako.WTAKODeath.Utils.ScoreboardUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathInfoListener implements Listener {

    private static HashMap<UUID, Long> playerDeathTimes = new HashMap<UUID, Long>();

    @EventHandler
    public static void onPlayerDeath(final PlayerDeathEvent event) {
        final Player victim = event.getEntity();
        PlayerDeathInfoListener.playerDeathTimes.put(victim.getUniqueId(), System.currentTimeMillis());
        final String deathMessage = PlayerDeathInfoListener.getDeathScoreboardMessage(victim);
        if (Main.getInstance().getConfig().getBoolean("DeathInfo.EnableLog")) {
            try {
                final FileWriter writer = new FileWriter(new File(Main.getInstance().getDataFolder(), "log.log"), true);
                writer.append(MessageFormat.format(Lang.LOG_FORMAT_DEATH.toString() + "\r\n",
                        new Date(System.currentTimeMillis()), event.getEntity().getName(),
                        deathMessage.replaceAll("^%lb%", "").replaceAll("%lb%$", "").replaceAll("(%lb%)+", "%lb%")
                                .replaceAll("%lb%", ", ")));
                writer.close();
            } catch (final IOException e) {
                event.getEntity().sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                e.printStackTrace();
            }
        }
        if (!Main.getInstance().getConfig().getBoolean("DeathInfo.Show")
                || !victim.hasPermission(Main.getInstance().getProperty("artifactId") + ".canHaveDeathInfo")) {
            return;
        }
        if (ScoreboardUtils.noShowScoreboardPlayers.contains(victim.getUniqueId())) {
            return;
        }
        ScoreboardUtils.showScoreboardMessage(Lang.DEATH_INFO_TITLE.toString(), deathMessage,
                Lang.DEATH_INFO_DELIMITER.toString(), victim,
                Main.getInstance().getConfig().getLong("DeathInfo.DelayTicks"),
                Main.getInstance().getConfig().getLong("DeathInfo.ShowTicks"));
    }

    @EventHandler
    public static void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("DeathInfo.Show")) {
            return;
        }
        final Player victim = event.getPlayer();
        if (ScoreboardUtils.noShowScoreboardPlayers.contains(victim.getUniqueId())) {
            return;
        }
        if (!PlayerDeathInfoListener.playerDeathTimes.containsKey(victim.getUniqueId())) {
            return;
        }
        final Long millisReduction = System.currentTimeMillis()
                - PlayerDeathInfoListener.playerDeathTimes.get(victim.getUniqueId());
        final Long ticksReduction = millisReduction / 50L;
        final String deathMessage = PlayerDeathInfoListener.getDeathScoreboardMessage(victim);
        ScoreboardUtils.showScoreboardMessage(Lang.DEATH_INFO_TITLE.toString(), deathMessage,
                Lang.DEATH_INFO_DELIMITER.toString(), victim,
                Main.getInstance().getConfig().getLong("DeathInfo.DelayTicks"),
                Main.getInstance().getConfig().getLong("DeathInfo.ShowTicks") - ticksReduction);
    }

    public static String getDeathScoreboardMessage(Player victim) {
        final String locationMsg = MessageFormat.format(Lang.DEATH_INFO_LOCATION_FORMAT.toString(),
                Main.getHumanTranslation(victim.getLocation().getWorld().getName()), victim.getLocation().getBlockX(),
                victim.getLocation().getBlockY(), victim.getLocation().getBlockZ())
                + Lang.DEATH_INFO_DELIMITER.toString();
        String killerMsg = "";
        if (victim.getKiller() != null && victim.getKiller() instanceof Player) {
            killerMsg = Lang.DEATH_INFO_KILLER.toString() + ": " + victim.getKiller().getName()
                    + Lang.DEATH_INFO_DELIMITER.toString();
        } else if (victim.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            killerMsg = Lang.DEATH_INFO_KILLER.toString()
                    + ": "
                    + Main.getHumanTranslation(((EntityDamageByEntityEvent) victim.getLastDamageCause()).getDamager()
                            .getType().name()) + Lang.DEATH_INFO_DELIMITER.toString();
        }
        final Date deathDate = new Date();
        deathDate.setTime(PlayerDeathInfoListener.playerDeathTimes.get(victim.getUniqueId()));
        final String timeMsg = Lang.DEATH_INFO_TIME.toString() + ": "
                + new SimpleDateFormat("HH:mm:ss").format(deathDate) + Lang.DEATH_INFO_DELIMITER.toString();
        String causeMsg;
        if (victim.getLastDamageCause() != null) {
            causeMsg = Lang.DEATH_INFO_CAUSE.toString() + ": "
                    + Main.getHumanTranslation(victim.getLastDamageCause().getCause().name())
                    + Lang.DEATH_INFO_DELIMITER.toString();
        } else {
            causeMsg = Lang.DEATH_INFO_CAUSE.toString() + ": " + Lang.DEATH_INFO_CAUSE_UNKNOWN.toString()
                    + Lang.DEATH_INFO_DELIMITER.toString();
        }
        final String deathMessage = MessageFormat.format(Lang.DEATH_INFO_FORMAT.toString(), locationMsg, killerMsg,
                timeMsg, causeMsg, Lang.DEATH_INFO_EXTRA_MSG.toString());
        return deathMessage;
    }
}
