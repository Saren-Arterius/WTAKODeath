package net.wtako.WTAKODeath.EventHandlers;

import java.text.MessageFormat;

import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.FactionUtils;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DeathGuardListener implements Listener {

    @EventHandler
    public static void onGuardGetDamage(NPCDamageByEntityEvent event) {
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (event.getNPC() == deathGuard.getDeathGuardNPC()) {
                if (!deathGuard.getDeathGuardNPC().isSpawned()) {
                    return;
                }
                if (event.getDamager() == null || !(event.getDamager() instanceof Player)) {
                    event.setCancelled(true);
                    return;
                }
                final Player attacker = (Player) event.getDamager();
                if (attacker == deathGuard.getOwner()) {
                    event.setDamage(Integer.MAX_VALUE);
                    return;
                }
                if (!Main.getInstance().getConfig().getBoolean("System.FactionsSupport")) {
                    event.setCancelled(true);
                    return;
                }
                if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Attack.Enable")
                        && attacker.hasPermission(Main.getInstance().getProperty("artifactId") + ".canAttackGuard")
                        && FactionUtils.canAttack(deathGuard.getOwner(), attacker)) {
                    event.setDamage(((Long) Math.round(event.getDamage()
                            * Main.getInstance().getConfig()
                                    .getDouble("InventoryProtection.DeathGuardSystem.Attack.DamageToSecondFactor")))
                            .intValue());
                    if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Attack.Notify")) {
                        deathGuard.notifyAttack(attacker.getName());
                    }
                } else if (Main.getInstance().getConfig()
                        .getBoolean("InventoryProtection.DeathGuardSystem.Bless.Enable")
                        && attacker.hasPermission(Main.getInstance().getProperty("artifactId") + ".canBlessGuard")
                        && FactionUtils.canBless(deathGuard.getOwner(), attacker)) {
                    deathGuard.blessBy(attacker);
                    event.setCancelled(true);
                } else {
                    attacker.sendMessage(MessageFormat.format(Lang.GUARD_NO_ATTACK_NOR_BLESS.toString(), deathGuard
                            .getOwner().getName()));
                    event.setCancelled(true);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public static void onGuardDie(NPCDeathEvent event) {
        DeathGuard attackedDeathGuard = null;
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (event.getNPC() == deathGuard.getDeathGuardNPC()) {
                attackedDeathGuard = deathGuard;
                if (attackedDeathGuard.getDeathGuardNPC().getBukkitEntity().getKiller() == attackedDeathGuard
                        .getOwner()) {
                    attackedDeathGuard.giveBack();
                } else {
                    attackedDeathGuard.destroy();
                }
                event.getDrops().clear();
                event.setDroppedExp(0);
                break;
            }
        }
        if (attackedDeathGuard != null) {
            DeathGuard.getAllDeathGuards().remove(attackedDeathGuard);
        }
    }

}
