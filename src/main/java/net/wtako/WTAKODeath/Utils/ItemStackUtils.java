package net.wtako.WTAKODeath.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtils {

    public static ArrayList<ArrayList<ItemStack>> getSampleOfItemStack(List<ItemStack> itemStacks, int percentage) {
        final ArrayList<ArrayList<ItemStack>> keepAndDropItemStacks = new ArrayList<ArrayList<ItemStack>>();
        final ArrayList<ItemStack> tempItemStacks = new ArrayList<ItemStack>();
        for (final ItemStack itemStack: itemStacks) {
            tempItemStacks.add(itemStack.clone());
        }
        percentage = percentage > 100 ? 100 : percentage;
        percentage = percentage < 0 ? 0 : percentage;
        if (percentage == 100) {
            keepAndDropItemStacks.add(tempItemStacks);
            keepAndDropItemStacks.add(new ArrayList<ItemStack>());
            return keepAndDropItemStacks;
        } else if (percentage == 0) {
            keepAndDropItemStacks.add(new ArrayList<ItemStack>());
            keepAndDropItemStacks.add(tempItemStacks);
            return keepAndDropItemStacks;
        }
        final int keepSize = Math.round(tempItemStacks.size() * (percentage / 100F));
        final ArrayList<ItemStack> keepItems = new ArrayList<ItemStack>();
        final Random random = new Random();
        while (keepItems.size() != keepSize) {
            keepItems.add(tempItemStacks.remove(random.nextInt(tempItemStacks.size())));
        }
        keepAndDropItemStacks.add(keepItems);
        keepAndDropItemStacks.add(tempItemStacks);
        return keepAndDropItemStacks;
    }

    public static void giveToPlayerOrDrop(ItemStack itemStack, Player player, Location location) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(location, itemStack);
        } else {
            player.getInventory().addItem(itemStack);
        }
    }
}
