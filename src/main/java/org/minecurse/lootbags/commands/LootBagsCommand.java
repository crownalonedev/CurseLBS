package org.minecurse.lootbags.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.chest.ChestMenu;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.menus.LootBagPreviewMenu;

@CommandAlias("lootbags|lootbag")
public class LootBagsCommand extends BaseCommand {
   @Default
   public void onDefault(Player player) {
      ChestMenu menu = new ChestMenu("All Loot Bags", 3);
      menu.fillSides(Button.PLACEHOLDER);
      menu.setButton(
         10,
         new Button(
            LootBagPlugin.getInstance().getManager().findLootBag("CAPTURE_POINT").getItemStack().clone(),
            (player1, clickInfo) -> new LootBagPreviewMenu(LootBagPlugin.getInstance().getManager().findLootBag("CAPTURE_POINT")).show(player1)
         )
      );
      menu.setButton(
         11,
         new Button(
            LootBagPlugin.getInstance().getManager().findLootBag("MINIGAMES").getItemStack().clone(),
            (player1, clickInfo) -> new LootBagPreviewMenu(LootBagPlugin.getInstance().getManager().findLootBag("MINIGAMES")).show(player1)
         )
      );
      menu.setButton(
         12,
         new Button(
            LootBagPlugin.getInstance().getManager().findLootBag("BOSS").getItemStack().clone(),
            (player1, clickInfo) -> new LootBagPreviewMenu(LootBagPlugin.getInstance().getManager().findLootBag("BOSS")).show(player1)
         )
      );
      menu.setButton(
         13,
         new Button(
            LootBagPlugin.getInstance().getManager().findLootBag("COMPETITIVE").getItemStack().clone(),
            (player1, clickInfo) -> new LootBagPreviewMenu(LootBagPlugin.getInstance().getManager().findLootBag("COMPETITIVE")).show(player1)
         )
      );
      menu.setButton(
         14,
         new Button(
            LootBagPlugin.getInstance().getManager().findLootBag("DELTA_SCORPI").getItemStack().clone(),
            (player1, clickInfo) -> new LootBagPreviewMenu(LootBagPlugin.getInstance().getManager().findLootBag("DELTA_SCORPI")).show(player1)
         )
      );
      menu.setButton(
         15,
         new Button(
            LootBagPlugin.getInstance().getManager().findLootBag("ALPHA_CENTAURI").getItemStack().clone(),
            (player1, clickInfo) -> new LootBagPreviewMenu(LootBagPlugin.getInstance().getManager().findLootBag("ALPHA_CENTAURI")).show(player1)
         )
      );
      menu.setButton(
         16,
         new Button(
            LootBagPlugin.getInstance().getManager().findLootBag("CHAMPION").getItemStack().clone(),
            (player1, clickInfo) -> new LootBagPreviewMenu(LootBagPlugin.getInstance().getManager().findLootBag("CHAMPION")).show(player1)
         )
      );
      menu.buildInventory();
      menu.show(player);
   }
}
