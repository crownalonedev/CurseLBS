package org.minecurse.lootbags.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.menus.hype.HypeBox;
import org.minecurse.lootbags.menus.hype.HypeBoxView;

@CommandAlias("supersecrethypeviewcommand")
public class HypeViewCommand extends BaseCommand {
   private final LootBagPlugin plugin;

   public HypeViewCommand(LootBagPlugin plugin) {
      this.plugin = plugin;
   }

   @Default
   public void onDefault(Player player, String uuid) {
      UUID parsedUUID = UUID.fromString(uuid);
      HypeBox box = this.plugin.getHypeManager().getByUUID(parsedUUID);
      if (box != null) {
         HypeBoxView view = new HypeBoxView(box);
         view.getInventory().open(player);
      }
   }
}
