package org.minecurse.lootbags.menus;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.paginated.PaginatedMenu;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.struct.CrateType;
import org.minecurse.lootbags.struct.LootBag;
import org.minecurse.lootmanager.utils.RetroUtils;

public class LootbagCategoryMenu {
   public void show(Player player) {
      PaginatedMenu menu = new PaginatedMenu("All Lootbox Categories", 3, 7);
      menu.fillSides(Button.PLACEHOLDER);

      for (CrateType type : CrateType.values()) {
         List<LootBag> bags = LootBagPlugin.getInstance().getManager().getLootBags().stream().filter(bag -> bag.getType() == type).collect(Collectors.toList());
         ItemBuilder builder = new ItemBuilder(Material.CHEST);
         builder.name("&c&l" + RetroUtils.capitalize(type.name().toLowerCase().replace('_', ' ')));
         builder.lore("");
         builder.lore("&f • Registered Bags: " + bags.size());
         builder.lore("");
         builder.lore("&fListed Bags: ");

         for (int i = 0; i < bags.size(); i++) {
            if (i <= 10) {
               LootBag bag2 = bags.get(i);
               builder.lore("&f&l * " + bag2.getDisplayName());
            }
         }

         builder.lore("");
         builder.lore("&7Click here to view this category.");
         menu.addButton(new Button(builder, (player1, clickInformation) -> new LootbagListMenu(type).show(player1)));
      }

      menu.buildInventory();
      menu.show(player);
   }
}
