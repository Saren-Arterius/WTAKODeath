package net.wtako.WTAKODeath.Utils;

import java.text.MessageFormat;
import net.wtako.WTAKODeath.Main;

import org.bukkit.entity.Player;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;

public class FactionUtils {

    public static boolean canAttack(Player victim, Player attacker) {
        try {
            if (!Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Attack.Enable")) {
                return false;
            }
            if (victim == null) {
                return true;
            }
            if (!UPlayer.get(attacker).hasFaction() || !UPlayer.get(victim).hasFaction()) {
                return Main.getInstance().getConfig()
                        .getBoolean("InventoryProtection.DeathGuardSystem.Attack.CanAttack.Wilderness");
            }
            final Faction attackerFaction = UPlayer.get(attacker).getFaction();
            final Faction victimFaction = UPlayer.get(victim).getFaction();
            if (Main.getInstance().getConfig()
                    .getBoolean("InventoryProtection.DeathGuardSystem.Attack.CanAttack.Enemy")
                    && attackerFaction.getRelationWish(victimFaction) == Rel.ENEMY) {
                return true;
            }
            if (Main.getInstance().getConfig()
                    .getBoolean("InventoryProtection.DeathGuardSystem.Attack.CanAttack.Neutral")
                    && attackerFaction.getRelationWish(victimFaction) == Rel.NEUTRAL) {
                return true;
            }
            return false;
        } catch (final Error e) {
            attacker.sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Factions"));
            e.printStackTrace();
            return false;
        }

    }

    public static boolean canBless(Player blessee, Player blesser) {
        try {
            if (!Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Bless.Enable")) {
                return false;
            }
            if (blessee == null) {
                return false;
            }
            if (!UPlayer.get(blesser).hasFaction() || !UPlayer.get(blessee).hasFaction()) {
                return Main.getInstance().getConfig()
                        .getBoolean("InventoryProtection.DeathGuardSystem.Bless.CanBless.Wilderness");
            }
            final Faction blesserFaction = UPlayer.get(blesser).getFaction();
            final Faction blesseeFaction = UPlayer.get(blessee).getFaction();
            if (Main.getInstance().getConfig()
                    .getBoolean("InventoryProtection.DeathGuardSystem.Bless.CanBless.SameFaction")
                    && blesserFaction == blesseeFaction) {
                return true;
            }
            if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Bless.CanBless.Ally")
                    && blesserFaction.getRelationWish(blesseeFaction) == Rel.ALLY) {
                return true;
            }
            if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Bless.CanBless.Truce")
                    && blesserFaction.getRelationWish(blesseeFaction) == Rel.TRUCE) {
                return true;
            }
            return false;
        } catch (final Error e) {
            blesser.sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Factions"));
            e.printStackTrace();
            return false;
        }
    }
}
