package org.minecurse.lootbags.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.settings.Settings;
import org.minecurse.lootbags.struct.LootBag;

public class LootBagSaleTask extends BukkitRunnable {
   public void run() {
      LootBag showcased = LootBagPlugin.getInstance().getManager().findShowcasedLootBag();
      if (showcased != null) {
         String line1 = Settings.saleBroadcastLine1.replace("%lootbag%", showcased.getDisplayName());
         String line2 = Settings.saleBroadcastLine2;

         for (Player all : Bukkit.getOnlinePlayers()) {
            all.sendMessage("");
            all.sendMessage(StringUtil.center(StringUtil.color(line1)));
            all.sendMessage(StringUtil.center(StringUtil.color(line2)));
            all.sendMessage("");
         }
      }
   }
}
