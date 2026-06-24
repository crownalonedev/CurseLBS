package org.minecurse.lootbags.tasks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.manager.HypeManager;
import org.minecurse.lootbags.settings.Settings;

public class HypeChatTask extends BukkitRunnable {
   private final HypeManager hypeManager;
   private final Set<UUID> redeemed = new HashSet<>();
   private int currentTick = 0;

   public HypeChatTask(HypeManager hypeManager) {
      this.hypeManager = hypeManager;
   }

   public void run() {
      if (this.currentTick >= 10) {
         Bukkit.broadcastMessage(LootBagPlugin.hypePrefix(Settings.hypeWaveEndedMessage));
         this.cancel();
         this.hypeManager.setHypeChatTask(null);
      } else {
         this.currentTick++;
      }
   }

   public Set<UUID> getRedeemed() {
      return this.redeemed;
   }
}
