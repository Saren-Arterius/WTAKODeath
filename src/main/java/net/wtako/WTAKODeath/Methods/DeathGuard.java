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

    private static ArrayList<DeathGuard> deathGuards     = new ArrayList<DeathGuard>();
    private final UUID                   ownerID;
    private final ArrayList<ItemStack>   itemStacks;
    private final double                 exp;
    private final NPC                    deathGuardNPC;
    private final BukkitRunnable         timer;
    private final HashMap<UUID, Integer> playerHits      = new HashMap<UUID, Integer>();
    private boolean                      endOfLife       = false;
    private double                       lastHealth;
    private long                         noNotifyUntil   = 0;
    private long                         noHitBackUntil  = 0;
    private String                       storedOwnerName;
    private String                       lastDamagerName = null;
    private Location                     lastStoredLocation;

    @SuppressWarnings("deprecation")
    public DeathGuard(final Player owner, ArrayList<ItemStack> itemStacks, double exp) {
        ownerID = owner.getUniqueId();
        this.itemStacks = itemStacks;
        this.exp = exp;
        final NPCRegistry registry = CitizensAPI.getNPCRegistry();
        deathGuardNPC = registry.createNPC(
                EntityType.valueOf(Main.getInstance().getConfig()
                        .getString("InventoryProtection.DeathGuardSystem.GuardEntityType").toUpperCase()),
                "Death Guard");
        lastStoredLocation = owner.getLocation();
        lastHealth = Main.getInstance().getConfig().getDouble("InventoryProtection.DeathGuardSystem.ProtectSeconds");
        deathGuardNPC.spawn(lastStoredLocation);
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (endOfLife) {
                    timer.cancel();
                    return;
                }
                lastHealth--;
                if (deathGuardNPC.isSpawned()) {
                    deathGuardNPC.setProtected(true);
                    deathGuardNPC.getBukkitEntity().damage(0);
                    deathGuardNPC.getBukkitEntity().setMaxHealth(
                            Main.getInstance().getConfig()
                                    .getDouble("InventoryProtection.DeathGuardSystem.ProtectSeconds"));
                    deathGuardNPC.getBukkitEntity().setHealth(lastHealth);
                    lastStoredLocation = deathGuardNPC.getBukkitEntity().getLocation();
                    storedOwnerName = getOwner() != null ? getOwner().getName() : storedOwnerName;
                    updateName();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            deathGuardNPC.setProtected(false);
                        }
                    }.runTaskLater(Main.getInstance(), 20L);
                } else {
                    if (lastHealth <= 0) {
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
            }
        };
        timer.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    @Override
    public String toString() {
        return MessageFormat.format(Lang.GUARD_TO_STRING.toString(), getOwnerName(),
                StringUtils.locationToString(lastStoredLocation), lastHealth);
    }

    public void updateName() {
        if (!deathGuardNPC.isSpawned()) {
            return;
        }
        deathGuardNPC.setName(MessageFormat.format(Lang.GUARD_NAME_FORMAT.toString(), getOwnerName(),
                Math.round(lastHealth),
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
            blesser.sendMessage(MessageFormat.format(Lang.GUARD_NOT_ENOUGH_EXP_BLESS.toString(), getOwnerName()));
            return;
        }
        manager.changeExp(-expCost);
        guardEntity.setHealth(guardEntity.getHealth() + blessSecond);
        lastHealth += blessSecond;
        blesser.sendMessage(MessageFormat.format(Lang.GUARD_BLESSED.toString(), getOwnerName(), Math.round(blessSecond)));
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.Bless.Notify")
                && getOwner() != null) {
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
            if (getOwner() != null && noNotifyUntil < System.currentTimeMillis()) {
                getOwner().sendMessage(
                        MessageFormat.format(Lang.GUARD_UNDER_ATTACK.toString(), toString(), attackerName));
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
            attacker.sendMessage(MessageFormat.format(Lang.GUARD_HIT_YOU_BACK.toString(), getOwnerName()));
            noHitBackUntil = System.currentTimeMillis() + millisInterval;
        }

    }

    @SuppressWarnings("deprecation")
    public void giveBack() {
        if (!isValid()) {
            return;
        }
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
        if (getOwner() != null) {
            getOwner().sendMessage(Lang.GUARD_GAVE_BACK.toString());
        }
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.EnableLog")) {
            try {
                final FileWriter writer = new FileWriter(new File(Main.getInstance().getDataFolder(), "log.log"), true);
                writer.append(MessageFormat.format(Lang.LOG_FORMAT_GUARD_GAVE_BACK.toString() + "\r\n",
                        new Date(System.currentTimeMillis()), toString()));
                writer.close();
            } catch (final IOException e) {
                getOwner().sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void destroy() {
        if (!isValid()) {
            return;
        }
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
        if (getOwner() != null) {
            getOwner().sendMessage(MessageFormat.format(Lang.GUARD_DIED.toString(), toString()));
        }
        if (Main.getInstance().getConfig().getBoolean("InventoryProtection.DeathGuardSystem.EnableLog")) {
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
                getOwner().sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Logger"));
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
        return Main.getInstance().getServer().getPlayer(ownerID);
    }

    public String getOwnerName() {
        return getOwner() != null ? getOwner().getName() : storedOwnerName != null ? storedOwnerName
                : Lang.OFFLINE_PLAYER.toString();
    }

    public void modifyLastHealth(double value) {
        double maxHealth = Main.getInstance().getConfig()
                .getDouble("InventoryProtection.DeathGuardSystem.ProtectSeconds");
        lastHealth = lastHealth + value < 0 ? 0 : lastHealth + value > maxHealth ? maxHealth : lastHealth + value;
    }

    public boolean isEndOfLife() {
        return endOfLife;
    }

    public boolean isValid() {
        if (endOfLife) {
            return false;
        }
        if (itemStacks == null) {
            return false;
        }
        return true;
    }

    public static void killAllDeathGuards() {
        final ArrayList<DeathGuard> needToKill = new ArrayList<DeathGuard>();
        for (final DeathGuard deathGuard: DeathGuard.getAllDeathGuards()) {
            if (deathGuard.getDeathGuardNPC().isSpawned()) {
                needToKill.add(deathGuard);
            }
        }
        for (final DeathGuard killDeathGuard: needToKill) {
            if (killDeathGuard.getOwner() != null && killDeathGuard.getOwner().isOnline()) {
                killDeathGuard.giveBack();
            } else {
                killDeathGuard.destroy();
            }
        }
        DeathGuard.getAllDeathGuards().clear();
        needToKill.clear();
    }

    public static ArrayList<DeathGuard> getAllDeathGuards() {
        return DeathGuard.deathGuards;
    }

}
