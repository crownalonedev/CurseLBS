package org.minecurse.lootbags.menus;

import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.paginated.PaginatedMenu;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.struct.CrateType;
import org.minecurse.lootbags.struct.LootBag;
import org.minecurse.lootmanager.utils.RetroUtils;

public class LootbagListMenu {
   private final CrateType toShow;

   public LootbagListMenu(CrateType bags) {
      this.toShow = bags;
   }

   public void show(Player player) {
      PaginatedMenu menu = new PaginatedMenu("All Available Lootbags", 6, 28);
      menu.fillSides(Button.PLACEHOLDER);

      for (LootBag bag : LootBagPlugin.getInstance().getManager().getLootBags().stream().filter(lb -> lb.getType() == this.toShow).collect(Collectors.toList())) {
         ItemBuilder builder = new ItemBuilder(bag.getLootBag());
         builder.lore("");
         builder.lore("&f • Internal Name: " + bag.getInternalName());
         builder.lore("&f • Bag Type: " + RetroUtils.capitalize(bag.getType().name().toLowerCase().replace('_', ' ')));
         builder.lore("");
         builder.lore("&7Left Click to receive Lootbag.");
         builder.lore("&7Right Click to edit Lootbag.");
         menu.addButton(new Button(builder, (player1, clickInformation) -> {
            if (clickInformation.getClickType().isLeftClick()) {
               player1.getInventory().addItem(new ItemStack[]{bag.getLootBag()});
            } else {
               new LootBagCreationMenu(bag).show(player1);
            }
         }));
      }

      menu.setButton(
         49,
         new Button(
            new ItemBuilder(Material.ARROW).name("&bGo Back").lore(new String[]{"&7Go back to select", "&7a new category."}),
            (player1, clickInformation) -> new LootbagCategoryMenu().show(player1)
         )
      );
      menu.buildInventory();
      menu.show(player);
   }
}
