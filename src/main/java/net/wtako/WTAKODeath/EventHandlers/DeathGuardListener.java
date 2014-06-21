package net.wtako.WTAKODeath.EventHandlers;

import java.text.MessageFormat;

import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Methods.DeathGuard;
import net.wtako.WTAKODeath.Utils.FactionUtils;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DeathGuardListener implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler
    public static void onGuardGetDamage(NPCDamageByEntityEvent event) {
        DeathGuard targetDeathGuard = null;
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (event.getNPC() == deathGuard.getDeathGuardNPC()) {
                targetDeathGuard = deathGuard;
                break;
            }
        }
        if (targetDeathGuard == null) {
            return;
        }
        if (!targetDeathGuard.getDeathGuardNPC().isSpawned()) {
            return;
        }
        if (event.getDamager() == null || !(event.getDamager() instanceof Player)) {
            event.setCancelled(true);
            return;
        }
        final Player attacker = (Player) event.getDamager();
        if (attacker == targetDeathGuard.getOwner()) {
            event.setDamage(Integer.MAX_VALUE);
            return;
        }
        if (!Main.getInstance().getConfig().getBoolean("System.FactionsSupport")) {
            event.setCancelled(true);
            return;
        }
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Attack.Enable")
                && attacker.hasPermission(Main.getInstance().getProperty("artifactId") + ".canAttackGuard")
                && FactionUtils.canAttack(targetDeathGuard.getOwner(), attacker)) {
            final int damage = ((Long) Math.round(event.getDamage()
                    * Main.getInstance().getConfig()
                            .getDouble("InventoryProtection.DeathGuardSystem.Attack.DamageToSecondFactor"))).intValue();
            event.setDamage(damage);
            targetDeathGuard.notifyAttack(attacker.getName());
            targetDeathGuard.hitBack(attacker);
            final double testHealth = targetDeathGuard.getDeathGuardNPC().getBukkitEntity().getHealth();
            final DeathGuard taskTargetDeathGuard = targetDeathGuard;
            Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    taskTargetDeathGuard.updateName();
                    if (taskTargetDeathGuard.getDeathGuardNPC().getBukkitEntity().getHealth() != testHealth) {
                        taskTargetDeathGuard.modifyLastHealth(-damage);
                    }
                }
            }, 1L);
            targetDeathGuard.updateName();
        } else if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Bless.Enable")
                && attacker.hasPermission(Main.getInstance().getProperty("artifactId") + ".canBlessGuard")
                && FactionUtils.canBless(targetDeathGuard.getOwner(), attacker)) {
            targetDeathGuard.blessBy(attacker);
            event.setCancelled(true);
        } else {
            attacker.sendMessage(MessageFormat.format(Lang.GUARD_NO_ATTACK_NOR_BLESS.toString(), targetDeathGuard
                    .getOwner().getName()));
            event.setCancelled(true);
        }

    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public static void onGuardGetDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        final LivingEntity livingEntity = (LivingEntity) event.getEntity();
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (deathGuard.getDeathGuardNPC().getBukkitEntity() == livingEntity
                    && !(event instanceof EntityDamageByEntityEvent)) {
                event.setCancelled(true);
                if (event.getCause() == DamageCause.SUFFOCATION) {
                    livingEntity.teleport(livingEntity.getLocation().add(1, 1, 1));
                }
                return;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public static void onGuardDie(NPCDeathEvent event) {
        DeathGuard targetDeathGuard = null;
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (event.getNPC() == deathGuard.getDeathGuardNPC()) {
                targetDeathGuard = deathGuard;
            }
        }
        if (targetDeathGuard != null) {
            if (targetDeathGuard.getDeathGuardNPC().getBukkitEntity().getKiller() == targetDeathGuard.getOwner()) {
                targetDeathGuard.giveBack();
            } else {
                targetDeathGuard.destroy();
            }
            event.getDrops().clear();
            event.setDroppedExp(0);
            DeathGuard.getAllDeathGuards().remove(targetDeathGuard);
        }
    }

}
