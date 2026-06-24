package org.minecurse.lootbags.menus;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.commons.utils.inventory.ClickableItem;
import org.minecurse.commons.utils.inventory.SmartInventory;
import org.minecurse.commons.utils.inventory.content.InventoryContents;
import org.minecurse.commons.utils.inventory.content.InventoryProvider;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.settings.Settings;
import org.minecurse.lootbags.struct.LootBag;

public class LootBagDisplayMenu implements InventoryProvider {
   private static final ItemStack NONE = new ItemBuilder(Material.BARRIER).name("&c&l???").lore("&7Check back later, there are no active loot boxes.");
   private static final SmartInventory inventory = SmartInventory.builder().title("Lootbag").size(3, 9).provider(new LootBagDisplayMenu()).build();

   public static SmartInventory getInventory() {
      return inventory;
   }

   public void init(Player player, InventoryContents contents) {
      contents.fill(ClickableItem.empty(new ItemBuilder(Material.STAINED_GLASS_PANE).name(" ").durability(7)));
      LootBag lootBag = LootBagPlugin.getInstance().getManager().findShowcasedLootBag();
      contents.set(1, 4, ClickableItem.of(lootBag == null ? NONE : lootBag.getItemStack(), event -> {
         player.sendMessage(StringUtil.color(Settings.displayMenuLockedLine1));
         player.sendMessage(StringUtil.color(Settings.displayMenuLockedLine2));
         PlayerUtils.playSound(player, Sound.ITEM_BREAK, 0.75F);
         player.closeInventory();
      }));
   }
}
