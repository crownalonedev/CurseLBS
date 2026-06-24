package org.minecurse.lootbags.menus.hype;

import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.commons.utils.inventory.ClickableItem;
import org.minecurse.commons.utils.inventory.SmartInventory;
import org.minecurse.commons.utils.inventory.content.InventoryContents;
import org.minecurse.commons.utils.inventory.content.InventoryProvider;

public class HypeBoxView implements InventoryProvider {
   private final HypeBox hypeBox;
   private final int tickEndingPoint;
   private final List<ItemStack> displayingItems;
   private final SmartInventory inventory;
   private float pitch;
   private int ticks;

   public HypeBoxView(HypeBox hypeBox) {
      this.hypeBox = hypeBox;
      this.tickEndingPoint = hypeBox.getTickEndingPoint();
      this.ticks = hypeBox.getTicks();
      this.displayingItems = hypeBox.getDisplayingItems();
      this.inventory = SmartInventory.builder()
         .id("hype-box-" + hypeBox.getOpener().getUniqueId())
         .provider(this)
         .size(3, 9)
         .title(StringUtil.color(hypeBox.getLootBag().getDisplayName() + " &8- " + hypeBox.getOpener().getDisplayName()))
         .build();
      this.pitch = 1.1F;
   }

   public void init(Player player, InventoryContents contents) {
      contents.fillBorders(ClickableItem.empty(HypeBox.getSIDES()));
      contents.set(4, ClickableItem.empty(this.hypeBox.getLootBag().getLootBag()));

      for (int i = 0; i < 9; i++) {
         int itemIndex = (this.displayingItems.size() - 1 - i + this.ticks) % this.displayingItems.size();
         contents.set(9 + i, ClickableItem.empty(this.displayingItems.get(itemIndex)));
      }
   }

   public void update(Player player, InventoryContents contents) {
      if (this.tickEndingPoint > this.ticks) {
         int diff = this.tickEndingPoint - this.ticks;
         int interval = diff >= 6 ? this.ticks / 8 + 1 : (diff >= 3 ? this.ticks / 3 + 1 : this.ticks / 2 + 1);
         int state = (Integer)contents.property("state", 0);
         contents.setProperty("state", state + 1);
         if (state % interval == 0) {
            PlayerUtils.playSound(player, Sound.NOTE_PLING, this.pitch);
            this.pitch += 0.02F;
            this.ticks++;
            this.init(player, contents);
         }
      }
   }

   public SmartInventory getInventory() {
      return this.inventory;
   }
}
