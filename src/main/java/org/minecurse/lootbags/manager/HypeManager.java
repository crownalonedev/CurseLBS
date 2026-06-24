package org.minecurse.lootbags.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.menus.hype.HypeBox;
import org.minecurse.lootbags.tasks.HypeChatTask;

public class HypeManager {
   private final List<HypeBox> activeHypeBoxes = new ArrayList<>();
   private final Set<UUID> openingHypeBoxes = new HashSet<>();
   private HypeChatTask hypeChatTask;

   public List<HypeBox> getActiveHypeBoxes() {
      return this.activeHypeBoxes;
   }

   public Set<UUID> getOpeningHypeBoxes() {
      return this.openingHypeBoxes;
   }

   public void setHypeChatTask(HypeChatTask hypeChatTask) {
      this.hypeChatTask = hypeChatTask;
   }

   public HypeChatTask getHypeChatTask() {
      return this.hypeChatTask;
   }

   public void createHypeChat() {
      if (this.hypeChatTask == null) {
         this.hypeChatTask = new HypeChatTask(this);
         this.hypeChatTask.runTaskTimerAsynchronously(LootBagPlugin.getInstance(), 0L, 20L);
      }
   }

   public void removePlayer(Player player) {
      this.openingHypeBoxes.remove(player.getUniqueId());
   }

   public void addPlayer(Player player) {
      this.openingHypeBoxes.add(player.getUniqueId());
   }

   public HypeBox getByUUID(UUID uuid) {
      return this.activeHypeBoxes.stream().filter(hypeBox -> hypeBox.getOpener().getUniqueId().equals(uuid)).findFirst().orElse(null);
   }
}
