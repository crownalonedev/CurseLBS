package org.minecurse.lootbags.menus.hype;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.commons.utils.inventory.ClickableItem;
import org.minecurse.commons.utils.inventory.SmartInventory;
import org.minecurse.commons.utils.inventory.content.InventoryContents;
import org.minecurse.commons.utils.inventory.content.InventoryProvider;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.menus.LootBagPreviewMenu;
import org.minecurse.lootbags.struct.CrateType;
import org.minecurse.lootbags.struct.LootBag;

public class HypeBoxListMenu implements InventoryProvider {
   private static final SmartInventory inventory = SmartInventory.builder()
      .id("hype-box-menu")
      .provider(new HypeBoxListMenu())
      .size(6, 9)
      .title(StringUtil.color("&8Hype Boxes"))
      .build();

   public static SmartInventory getInventory() {
      return inventory;
   }

   public void init(Player player, InventoryContents contents) {
   }

   public void update(Player player, InventoryContents contents) {
      int state = (Integer)contents.property("state", 0);
      contents.setProperty("state", state + 1);
      if (state % 11 == 0) {
         contents.fillBorders(ClickableItem.empty(HypeBox.getSIDES().clone()));
         int i = 10;

         for (LootBag hype : LootBagPlugin.getInstance().getManager().getLootBags()) {
            if (hype.getType() == CrateType.HYPE_BOX) {
               contents.set(i, ClickableItem.of(hype.getLootBag(), event -> {
                  if (player.hasPermission("curse.admin")) {
                     player.getInventory().addItem(new ItemStack[]{hype.getLootBag()});
                     PlayerUtils.playSound(player, Sound.CHICKEN_EGG_POP, 1.0F);
                  } else {
                     new LootBagPreviewMenu(hype).show(player);
                  }
               }));
               i++;
            }
         }
      }
   }
}
