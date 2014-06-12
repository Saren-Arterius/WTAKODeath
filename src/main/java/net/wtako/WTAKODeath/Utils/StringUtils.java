package net.wtako.WTAKODeath.Utils;

import org.bukkit.ChatColor;

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

}
