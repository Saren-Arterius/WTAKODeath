package net.wtako.WTAKODeath.Utils;

import net.wtako.WTAKODeath.Main;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * An enum for requesting strings from the language file.
 * 
 * @author gomeow
 */
public enum Lang {

    TITLE("title", "[" + Main.getInstance().getName() + "]"),

    DEATH_INFO_TITLE("death-info-title", "Death info"),
    DEATH_INFO_LOCATION_FORMAT("death-info-location-format", "World: {0}%lb%X: {1}%lb%Y: {2}%lb%Z: {3}"),
    DEATH_INFO_KILLER("death-info-killer", "Killer"),
    DEATH_INFO_TIME("death-info-time", "Time"),
    DEATH_INFO_CAUSE("death-info-cause", "Cause"),
    DEATH_INFO_CAUSE_UNKNOWN("death-info-cause-unknown", "unknown"),
    DEATH_INFO_EXTRA_MSG("death-info-extra-msg", "Extra message%lb%lies here."),
    DEATH_INFO_FORMAT("death-info-format", "%lb%{0}{1}{2}{3}%lb%{4}"),
    DEATH_INFO_DELIMITER("death-info-delimiter", "%lb%"),

    DEATH_INFO_NOT_DISPLAYING("death-info-not-displaying", "&eDeath info is not displaying."),
    DEATH_INFO_REMOVED("death-info-removed", "&aDeath info removed."),
    DEATH_INFO_NO_LONGER_SHOW("death-info-no-longer-show", "&aDeath info will no longer be shown until server restart."),
    DEATH_INFO_SHOW_AGAIN("death-info-show-again", "&aDeath info will show upon your death."),

    GUARD_NAME_FORMAT("guard-name-format", "{0}''s Guard ({1}/{2})"),
    GUARD_TO_STRING("guard-to-string", "Death Guard (Owner: {0}, {1}, Second(s) left: {2})"),
    GUARD_SPAWN("guard-spawn", "&aA {0} is spawned for you, click on it to get back all your missing items and exp."),
    GUARD_DIED("guard-died", "&eYour {0} has passed away and dropped all the items and exp that are kept for you..."),
    GUARD_GAVE_BACK("guard-gave-back", "&aThe Guard gave back all of your items and some of your exp, then it left..."),
    GUARD_NO_ATTACK_NOR_BLESS("guard-no-attack-nor-bless", "&cYou can not attack nor bless {0}''s Guard."),
    GUARD_BLESSED("guard-blessed", "&aYou blessed {0}''s Guard, extended its life for {1} seconds."),
    GUARD_GET_BLESSED("guard-get-blessed", "&aYour {0} has been blessed by {1}, extended its life for {2} seconds."),
    GUARD_UNDER_ATTACK("guard-under-attack", "&cYour {0} is under attack! (Attacker: {1})"),
    GUARD_NOT_ENOUGH_EXP_BLESS("guard-not-enough-exp-bless", "&eYou do not have enough exp to bless {0}''s Guard."),
    GUARD_HIT_YOU_BACK("guard-hit-you-back", "&e{0}''s Guard hit you back with its wrath!"),
    OFFLINE_PLAYER("offline-player", "Offline player"),
    ALL_GUARDS_KILLED("all-guards-killed", "&aSuccessfully killed all death guards."),
    YOU_DONT_HAVE_GUARDS("you-dont-have-guards", "&eYou do not have any death guards."),
    NO_GUARDS("no-guards", "&eCurrently there is no guard in this server."),
    YOU_KEPT_ITEMS_LEVELS(
            "you-kept-items-exp",
            "&e{0} item(s) and {1} level(s) are kept. Items will be returned to you upon your respawn."),
    HELP_GUARDS("help-guards", "Type &a/" + Main.getInstance().getProperty("mainCommand").toLowerCase()
            + " guards&f to view all of your death guards."),
    HELP_OFF("help-off", "Type &a/" + Main.getInstance().getProperty("mainCommand").toLowerCase()
            + " off&f to remove the display board."),
    HELP_TSH("help-tsh", "Type &a/" + Main.getInstance().getProperty("mainCommand").toLowerCase()
            + " tsh&f to toggle death info showing on death."),
    HELP_ALL_GUARDS("help-all-guards", "Type &a/" + Main.getInstance().getProperty("mainCommand").toLowerCase()
            + " allguards&f to list all the death guards. &c(OP only)"),
    HELP_CLEAR("help-clear", "Type &a/" + Main.getInstance().getProperty("mainCommand").toLowerCase()
            + " clear&f to kill (Give back or drop if owner offline) all the death guards. &c(OP only)"),
    HELP_RELOAD("help-reload", "Type &a/" + Main.getInstance().getProperty("mainCommand").toLowerCase()
            + " reload&f to reload the plugin. &c(OP only)"),
    LOCATION_FORMAT("location-format", "World: {0}, X: {1}, Y: {2}, Z: {3}"),
    LOG_FORMAT_DEATH("log-format-death", "[{0}] {1} died. ({2})"),
    LOG_FORMAT_ITEM_KEPT("log-format-item-kept", "[{0}] {1} kept {2}."),
    LOG_FORMAT_ITEM_DROPPED("log-format-item-drop", "[{0}] {1} dropped/guarded {2}. ({3})"),
    LOG_FORMAT_EXP_KEPT_GUARDED(
            "log-format-exp-kept-guarded",
            "[{0}] {1} kept {2} exp (and guarded {3}). (Before death: {4} exp)"),
    LOG_FORMAT_GUARD_GAVE_BACK("log-format-guard-gave-back", "[{0}] {1} gaved back."),
    LOG_FORMAT_GUARD_DESTROY("log-format-guard-destroy", "[{0}] {1} get destroyed."),
    LOG_FORMAT_GUARD_KILLED("log-format-guard-killed", "[{0}] {1} last damaged (killed) by {2}."),
    ERROR_HOOKING("error-hooking", "&4Error in hooking into {0}! Please contact server administrators."),
    NO_PERMISSION_COMMAND("no-permission-command", "&cYou are not allowed to use this command."),
    PLUGIN_RELOADED("plugin-reloaded", "&aPlugin reloaded.");

    private String                   path;
    private String                   def;
    private static YamlConfiguration LANG;

    /**
     * Lang enum constructor.
     * 
     * @param path
     *            The string path.
     * @param start
     *            The default string.
     */
    Lang(String path, String start) {
        this.path = path;
        def = start;
    }

    /**
     * Set the {@code YamlConfiguration} to use.
     * 
     * @param config
     *            The config to set.
     */
    public static void setFile(YamlConfiguration config) {
        Lang.LANG = config;
    }

    @Override
    public String toString() {
        if (this == TITLE) {
            return ChatColor.translateAlternateColorCodes('&', Lang.LANG.getString(path, def)) + " ";
        }
        return ChatColor.translateAlternateColorCodes('&', Lang.LANG.getString(path, def));
    }

    /**
     * Get the default value of the path.
     * 
     * @return The default value of the path.
     */
    public String getDefault() {
        return def;
    }

    /**
     * Get the path to the string.
     * 
     * @return The path to the string.
     */
    public String getPath() {
        return path;
    }
}