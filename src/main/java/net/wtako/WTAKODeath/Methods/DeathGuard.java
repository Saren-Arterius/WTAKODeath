package net.wtako.WTAKODeath.Methods;

import java.text.MessageFormat;
import java.util.ArrayList;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Utils.ExperienceManager;
import net.wtako.WTAKODeath.Utils.ItemStackUtils;
import net.wtako.WTAKODeath.Utils.Lang;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathGuard implements Listener {

    private static ArrayList<DeathGuard> deathGuards   = new ArrayList<DeathGuard>();
    private final Player                 owner;
    private final ArrayList<ItemStack>   itemStacks;
    private final double                 exp;
    private final NPC                    deathGuardNPC;
    private final BukkitRunnable         timer;
    private long                         noNotifyUntil = 0;
    private Location                     latestLocation;

    @SuppressWarnings("deprecation")
    public DeathGuard(final Player owner, ArrayList<ItemStack> itemStacks, double exp) {
        this.owner = owner;
        this.itemStacks = itemStacks;
        this.exp = exp;
        final NPCRegistry registry = CitizensAPI.getNPCRegistry();
        deathGuardNPC = registry.createNPC(
                EntityType.valueOf(Main.getInstance().getConfig()
                        .getString("InventoryProtection.DeathGuardSystem.GuardEntityType").toUpperCase()),
                "Death Guard");
        deathGuardNPC.setProtected(false);
        deathGuardNPC.spawn(owner.getLocation());
        latestLocation = owner.getLocation();
        deathGuardNPC.getBukkitEntity().setMaxHealth(
                Main.getInstance().getConfig().getDouble("InventoryProtection.DeathGuardSystem.ProtectSeconds"));
        deathGuardNPC.getBukkitEntity().setHealth(
                Main.getInstance().getConfig().getDouble("InventoryProtection.DeathGuardSystem.ProtectSeconds"));
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (!deathGuardNPC.isSpawned()) {
                    timer.cancel();
                }
                latestLocation = deathGuardNPC.getBukkitEntity().getLocation();
                deathGuardNPC.getBukkitEntity().damage(1);
                deathGuardNPC.setName(MessageFormat.format(Lang.GUARD_NAME_FORMAT.toString(), owner.getName(),
                        Math.round(deathGuardNPC.getBukkitEntity().getHealth()),
                        Main.getInstance().getConfig().getInt("InventoryProtection.DeathGuardSystem.ProtectSeconds")));
            }
        };
        timer.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        return MessageFormat.format(Lang.GUARD_TO_STRING.toString(), owner.getName(), Main
                .getHumanTranslation(latestLocation.getWorld().getName()), latestLocation.getBlockX(), latestLocation
                .getBlockY(), latestLocation.getBlockZ(), deathGuardNPC.isSpawned() ? deathGuardNPC.getBukkitEntity()
                .getHealth() : 0);
    }

    @SuppressWarnings("deprecation")
    public void giveBack() {
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.KeepDroppedItems")) {
            for (final ItemStack itemStack: itemStacks) {
                ItemStackUtils.giveToPlayerOrDrop(itemStack, getOwner(), getOwner().getLocation());
            }
        }
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.KeepLostExp")) {
            final ExperienceManager manager = new ExperienceManager(getOwner());
            manager.changeExp(exp);
        }
        if (deathGuardNPC.isSpawned()) {
            deathGuardNPC.getBukkitEntity().damage(Integer.MAX_VALUE);
        }
        final NPCRegistry registry = CitizensAPI.getNPCRegistry();
        registry.deregister(deathGuardNPC);
        getOwner().sendMessage(Lang.GUARD_GAVE_BACK.toString());
    }

    @SuppressWarnings("deprecation")
    public void destroy() {
        final World world = latestLocation.getWorld();
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.KeepDroppedItems")) {
            for (final ItemStack itemStack: itemStacks) {
                world.dropItemNaturally(latestLocation, itemStack);
            }
        }
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.KeepLostExp")) {
            world.spawn(latestLocation, ExperienceOrb.class).setExperience(((Long) Math.round(exp)).intValue());
        }
        if (deathGuardNPC.isSpawned()) {
            deathGuardNPC.getBukkitEntity().damage(Integer.MAX_VALUE);
        }
        final NPCRegistry registry = CitizensAPI.getNPCRegistry();
        registry.deregister(deathGuardNPC);
        getOwner().sendMessage(MessageFormat.format(Lang.GUARD_DIED.toString(), toString()));
    }

    @SuppressWarnings("deprecation")
    public void blessBy(Player blesser) {
        if (!deathGuardNPC.isSpawned()) {
            return;
        }
        final LivingEntity guardEntity = getDeathGuardNPC().getBukkitEntity();
        final double maxBlessSecond = Main.getInstance().getConfig()
                .getDouble("InventoryProtection.DeathGuardSystem.Bless.EachBlessSecondMax");
        final double lostSecond = guardEntity.getMaxHealth() - guardEntity.getHealth();
        final double blessSecond = lostSecond > maxBlessSecond ? maxBlessSecond : lostSecond;
        final double expCost = Main.getInstance().getConfig()
                .getDouble("InventoryProtection.DeathGuardSystem.Bless.EachSecondCostExp")
                * blessSecond;
        final ExperienceManager manager = new ExperienceManager(blesser);
        if (!manager.hasExp(expCost)) {
            blesser.sendMessage(MessageFormat.format(Lang.GUARD_NOT_ENOUGH_EXP_BLESS.toString(), getOwner().getName()));
            return;
        }
        manager.changeExp(-expCost);
        guardEntity.setHealth(guardEntity.getHealth() + blessSecond);
        blesser.sendMessage(MessageFormat.format(Lang.GUARD_BLESSED.toString(), getOwner().getName(),
                Math.round(blessSecond)));
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Bless.Notify")) {
            getOwner()
                    .sendMessage(
                            MessageFormat.format(Lang.GUARD_GET_BLESSED.toString(), blesser.getName(),
                                    Math.round(blessSecond)));
        }
    }

    public void notifyAttack(String attackerName) {
        final long millisInterval = Main.getInstance().getConfig()
                .getLong("InventoryProtection.DeathGuardSystem.Attack.NotifySecondsInterval") * 1000;
        if (noNotifyUntil < System.currentTimeMillis()) {
            owner.sendMessage(MessageFormat.format(Lang.GUARD_UNDER_ATTACK.toString(), toString(), attackerName));
            noNotifyUntil = System.currentTimeMillis() + millisInterval;
        }
    }

    public NPC getDeathGuardNPC() {
        return deathGuardNPC;
    }

    public Player getOwner() {
        return owner;
    }

    public static void killAllDeathGuards() {
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (deathGuard.getDeathGuardNPC().isSpawned()) {
                if (deathGuard.getOwner().isOnline()) {
                    deathGuard.giveBack();
                } else {
                    deathGuard.destroy();
                }
            }
        }
        DeathGuard.getAllDeathGuards().clear();
    }

    public static ArrayList<DeathGuard> getAllDeathGuards() {
        return DeathGuard.deathGuards;
    }

}
