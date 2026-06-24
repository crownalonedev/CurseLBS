package org.minecurse.lootbags.struct;

import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.item.ItemBuilder;

public enum HalfType {
   LEFT(
      "&a&lLeft Half (%display%&a&l)",
      Lists.newArrayList(
         new String[]{
            "&7Find my other counterpart to",
            "&7complete this puzzle.",
            "",
            "&a&lINFORMATION",
            " &a• &fLoot Bag: &f%display%",
            " &a• &fHalf Type: &aLeft",
            "",
            "&7&oDrag 'n Drop onto the correct half to form a Loot Bag!"
         }
      ),
      ChatColor.GREEN
   ),
   RIGHT(
      "&e&lRight Half (%display%&e&l)",
      Lists.newArrayList(
         new String[]{
            "&7Find my other counterpart to",
            "&7complete this puzzle.",
            "",
            "&e&lINFORMATION",
            " &e• &fLoot Bag: &f%display%",
            " &e• &fHalf Type: &eRight",
            "",
            "&7&oDrag 'n Drop onto the correct half to form a Loot Bag!"
         }
      ),
      ChatColor.YELLOW
   );

   private final String displayName;
   private final List<String> lore;
   private final ChatColor color;

   HalfType(String displayName, List<String> lore, ChatColor color) {
      this.displayName = displayName;
      this.lore = lore;
      this.color = color;
   }

   public static ItemStack build(LootBag lootBag, HalfType type) {
      ItemBuilder builder = new ItemBuilder(Material.PRISMARINE_SHARD);
      builder.name(type.getDisplayName().replace("%display%", lootBag.getDisplayName()));

      for (String string : type.getLore()) {
         builder.lore(string.replace("%display%", lootBag.getDisplayName()));
      }

      NBTItem finalItem = new NBTItem(builder);
      finalItem.setString("halfType", type.name());
      finalItem.setString("halfLootBag", lootBag.getInternalName());
      return finalItem.getItem();
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public List<String> getLore() {
      return this.lore;
   }

   public ChatColor getColor() {
      return this.color;
   }
}
