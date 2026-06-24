package org.minecurse.lootbags.utils;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.struct.HalfType;
import org.minecurse.lootbags.struct.LootBag;

public class LootBagUtils {
   public static boolean isLootBag(ItemStack item) {
      return item != null && !item.getType().equals(Material.AIR) ? new NBTItem(item).hasKey("lootBagType") : false;
   }

   public static LootBag getLootBag(ItemStack item) {
      return !isLootBag(item) ? null : LootBagPlugin.getInstance().getManager().getByName(new NBTItem(item).getString("lootBagType"));
   }

   public static boolean isHalf(ItemStack itemStack) {
      return itemStack != null && !itemStack.getType().equals(Material.AIR) ? new NBTItem(itemStack).hasKey("halfType") : false;
   }

   public static HalfType getHalfType(ItemStack item) {
      return !isHalf(item) ? null : HalfType.valueOf(new NBTItem(item).getString("halfType"));
   }

   public static LootBag getHalfLootBox(ItemStack item) {
      return !isHalf(item) ? null : LootBagPlugin.getInstance().getManager().getByName(new NBTItem(item).getString("halfLootBag"));
   }
}
