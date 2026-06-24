package org.minecurse.lootbags.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.utils.PlayerUtils;

public class DropUtils {
   public static void giveOrDropProtectedItem(Player player, ItemStack item) {
      if (player.getInventory().firstEmpty() != -1) {
         player.getInventory().addItem(new ItemStack[]{item});
      } else {
         player.getWorld().dropItemNaturally(player.getLocation(), item);
         PlayerUtils.playSound(player, Sound.CHICKEN_EGG_POP, 1.0F);
      }
   }
}