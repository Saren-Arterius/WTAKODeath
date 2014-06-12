package net.wtako.WTAKODeath.Methods;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.wtako.WTAKODeath.Main;
import net.wtako.WTAKODeath.Utils.ExperienceManager;
import net.wtako.WTAKODeath.Utils.ItemStackUtils;
import net.wtako.WTAKODeath.Utils.Lang;
import net.wtako.WTAKODeath.Utils.StringUtils;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathGuard implements Listener {

    private static ArrayList<DeathGuard> deathGuards     = new ArrayList<DeathGuard>();
    private final Player                 owner;
    private final ArrayList<ItemStack>   itemStacks;
    private final double                 exp;
    private final NPC                    deathGuardNPC;
    private final BukkitRunnable         timer;
    private final HashMap<UUID, Integer> playerHits      = new HashMap<UUID, Integer>();
    private boolean                      endOfLife       = false;
    private double                       lastHealth;
    private long                         noNotifyUntil   = 0;
    private long                         noHitBackUntil  = 0;
    private String                       lastDamagerName = null;

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
        deathGuardNPC.getBukkitEntity().setMaxHealth(
                Main.getInstance().getConfig().getDouble("InventoryProtection.DeathGuardSystem.ProtectSeconds"));
        deathGuardNPC.getBukkitEntity().setHealth(
                Main.getInstance().getConfig().getDouble("InventoryProtection.DeathGuardSystem.ProtectSeconds"));
        lastHealth = deathGuardNPC.getBukkitEntity().getHealth();
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (!deathGuardNPC.isSpawned() && endOfLife) {
                    timer.cancel();
                } else if (!deathGuardNPC.isSpawned() && !endOfLife) {
                    deathGuardNPC.spawn(deathGuardNPC.getStoredLocation());
                    deathGuardNPC.getBukkitEntity().setMaxHealth(
                            Main.getInstance().getConfig()
                                    .getDouble("InventoryProtection.DeathGuardSystem.ProtectSeconds"));
                    deathGuardNPC.getBukkitEntity().setHealth(lastHealth);
                }
                deathGuardNPC.getBukkitEntity().damage(1);
                // AKA chunk is unloaded
                if (lastHealth == deathGuardNPC.getBukkitEntity().getHealth()) {
                    deathGuardNPC.getBukkitEntity().setHealth(lastHealth - 1);
                    // Specially handle unloaded death guards
                    if (deathGuardNPC.getBukkitEntity().getHealth() <= 0) {
                        destroy();
                        ArrayList<DeathGuard> needToRemove = new ArrayList<DeathGuard>();
                        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
                            if (deathGuard.isEndOfLife()) {
                                needToRemove.add(deathGuard);
                            }
                        }
                        for (final DeathGuard deathGuard: needToRemove) {
                            DeathGuard.getAllDeathGuards().remove(deathGuard);
                        }
                        needToRemove.clear();
                        needToRemove = null;
                    }
                }
                lastHealth = deathGuardNPC.getBukkitEntity().getHealth();
                updateName();
            }
        };
        timer.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        return MessageFormat.format(Lang.GUARD_TO_STRING.toString(), owner.getName(), StringUtils
                .locationToString(deathGuardNPC.getStoredLocation()), deathGuardNPC.isSpawned() ? deathGuardNPC
                .getBukkitEntity().getHealth() : 0);
    }

    @SuppressWarnings("deprecation")
    public void updateName() {
        if (!deathGuardNPC.isSpawned()) {
            return;
        }
        deathGuardNPC.setName(MessageFormat.format(Lang.GUARD_NAME_FORMAT.toString(), owner.getName(),
                Math.round(deathGuardNPC.getBukkitEntity().getHealth()),
                Main.getInstance().getConfig().getInt("InventoryProtection.DeathGuardSystem.ProtectSeconds")));
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
        blesser.sendMessage(MessageFormat.format(Lang.GUARD_BLESSED.toString(), toString(), getOwner().getName(),
                Math.round(blessSecond)));
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Bless.Notify")) {
            getOwner()
                    .sendMessage(
                            MessageFormat.format(Lang.GUARD_GET_BLESSED.toString(), blesser.getName(),
                                    Math.round(blessSecond)));
        }
    }

    public void notifyAttack(String attackerName) {
        lastDamagerName = attackerName;
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Attack.Notify")) {
            final long millisInterval = Main.getInstance().getConfig()
                    .getLong("InventoryProtection.DeathGuardSystem.Attack.NotifySecondsInterval") * 1000;
            if (noNotifyUntil < System.currentTimeMillis()) {
                owner.sendMessage(MessageFormat.format(Lang.GUARD_UNDER_ATTACK.toString(), toString(), attackerName));
                noNotifyUntil = System.currentTimeMillis() + millisInterval;
            }
        }
    }

    public void hitBack(Player attacker) {
        deathGuardNPC.faceLocation(attacker.getEyeLocation());
        if (!Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Attack.HitBack.Enable")) {
            return;
        }
        if (!playerHits.containsKey(attacker.getUniqueId())) {
            playerHits.put(attacker.getUniqueId(), 1);
        } else {
            playerHits.put(attacker.getUniqueId(), playerHits.get(attacker.getUniqueId()) + 1);
        }
        if (playerHits.get(attacker.getUniqueId()) <= Main.getInstance().getConfig()
                .getInt("InventoryProtection.DeathGuardSystem.Attack.HitBack.TolerateHits")) {
            return;
        }
        final long millisInterval = Main.getInstance().getConfig()
                .getLong("InventoryProtection.DeathGuardSystem.Attack.HitBack.SecondsInterval") * 1000;
        if (noHitBackUntil < System.currentTimeMillis()) {
            if (Main.getInstance().getConfig()
                    .getBoolean("InventoryProtection.DeathGuardSystem.Attack.HitBack.LightningEffect")) {
                attacker.getWorld().strikeLightningEffect(attacker.getLocation());
            }
            attacker.damage(Main.getInstance().getConfig()
                    .getDouble("InventoryProtection.DeathGuardSystem.Attack.HitBack.Damage"));
            if (Main.getInstance().getConfig()
                    .getBoolean("InventoryProtection.DeathGuardSystem.Attack.HitBack.SetAttackerOnFire")) {
                attacker.setFireTicks(Main.getInstance().getConfig()
                        .getInt("InventoryProtection.DeathGuardSystem.Attack.HitBack.FireTicks"));
            }
            attacker.sendMessage(MessageFormat.format(Lang.GUARD_HIT_YOU_BACK.toString(), owner.getName()));
            noHitBackUntil = System.currentTimeMillis() + millisInterval;
        }

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
        cleanUp();
        getOwner().sendMessage(Lang.GUARD_GAVE_BACK.toString());
        if (Main.getInstance().getConfig().getBoolean("DeathInfo.DeathGuardSystem.EnableLog")) {
            try {
                final FileWriter writer = new FileWriter(new File(Main.getInstance().getDataFolder(), "log.log"), true);
                writer.append(MessageFormat.format(Lang.LOG_FORMAT_GUARD_GAVE_BACK.toString() + "\r\n",
                        new Date(System.currentTimeMillis()), toString()));
                writer.close();
            } catch (final IOException e) {
                owner.sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void destroy() {
        final World world = deathGuardNPC.getStoredLocation().getWorld();
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.KeepDroppedItems")) {
            for (final ItemStack itemStack: itemStacks) {
                world.dropItemNaturally(deathGuardNPC.getStoredLocation(), itemStack);
            }
        }
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.KeepLostExp")) {
            world.spawn(deathGuardNPC.getStoredLocation(), ExperienceOrb.class).setExperience(
                    ((Long) Math.round(exp)).intValue());
        }
        if (deathGuardNPC.isSpawned()) {
            deathGuardNPC.getBukkitEntity().damage(Integer.MAX_VALUE);
        }
        final NPCRegistry registry = CitizensAPI.getNPCRegistry();
        registry.deregister(deathGuardNPC);
        cleanUp();
        getOwner().sendMessage(MessageFormat.format(Lang.GUARD_DIED.toString(), toString()));
        if (Main.getInstance().getConfig().getBoolean("DeathInfo.DeathGuardSystem.EnableLog")) {
            try {
                final FileWriter writer = new FileWriter(new File(Main.getInstance().getDataFolder(), "log.log"), true);
                if (lastDamagerName != null) {
                    writer.append(MessageFormat.format(Lang.LOG_FORMAT_GUARD_KILLED.toString() + "\r\n", new Date(
                            System.currentTimeMillis()), toString(), lastDamagerName));
                }
                writer.append(MessageFormat.format(Lang.LOG_FORMAT_GUARD_DESTROY.toString() + "\r\n",
                        new Date(System.currentTimeMillis()), toString()));
                writer.close();
            } catch (final IOException e) {
                owner.sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                e.printStackTrace();
            }
        }
    }

    private void cleanUp() {
        playerHits.clear();
        itemStacks.clear();
        endOfLife = true;
    }

    public NPC getDeathGuardNPC() {
        return deathGuardNPC;
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isEndOfLife() {
        return endOfLife;
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
