package net.wtako.WTAKODeath.Utils;

import java.text.MessageFormat;

import net.wtako.WTAKODeath.Main;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class StringUtils {

    public static String toInvisible(String s) {
        String hidden = "";
        for (final char c: s.toCharArray()) {
            hidden += ChatColor.COLOR_CHAR + "" + c;
        }
        return hidden;
    }

    public static String fromInvisible(String s) {
        return s.replaceAll("§", "");
    }

    public static String locationToString(Location location) {
        return MessageFormat.format(Lang.LOCATION_FORMAT.toString(),
                Main.getHumanTranslation(location.getWorld().getName()), location.getBlockX(), location.getBlockY(),
                location.getBlockZ());
    }
}
