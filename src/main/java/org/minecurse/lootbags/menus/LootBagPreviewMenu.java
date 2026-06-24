package org.minecurse.lootbags.menus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.paginated.PaginatedMenu;
import org.minecurse.lootbags.struct.LootBag;
import org.minecurse.lootmanager.struct.Reward;

public class LootBagPreviewMenu {
   private final LootBag lootBag;
   private final PaginatedMenu inventory;

   public LootBagPreviewMenu(LootBag lootBag) {
      this.lootBag = lootBag;
      this.inventory = new PaginatedMenu(lootBag.getDisplayName() + " &7Rewards (" + lootBag.getRewards().size() + "&7)", 6, 28);
   }

   public void show(Player player) {
      this.inventory.fillSides(Button.PLACEHOLDER);

      for (Reward reward : new ArrayList<>(this.lootBag.getRewards()).stream().sorted(Comparator.comparing(Reward::getChance)).collect(Collectors.toList())) {
         ItemBuilder builder = new ItemBuilder(reward.getItemStack());
         this.inventory.addButton(new Button(builder, (player1, clickInformation) -> {
            if (player.hasPermission("curse.admin")) {
               player.getInventory().addItem(new ItemStack[]{reward.getItemStack()});
            }
         }));
      }

      this.inventory.buildInventory();
      this.inventory.show(player);
   }

   public PaginatedMenu getInventory() {
      return this.inventory;
   }
}
