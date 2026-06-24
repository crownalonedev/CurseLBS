package org.minecurse.lootbags.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.minecurse.lootbags.struct.EditorData;
import org.minecurse.lootbags.struct.LootBag;
import org.minecurse.lootmanager.struct.Reward;

public class EditorManager {
   private final Map<UUID, EditorData> rewards = new HashMap<>();

   public void addReward(Player player, LootBag lootBag, Reward reward) {
      EditorData editorData = this.rewards.getOrDefault(player.getUniqueId(), new EditorData(lootBag, new ArrayList<>()));
      editorData.getRewardList().add(reward);
      this.rewards.put(player.getUniqueId(), editorData);
   }

   public void removeReward(Player player, Reward reward) {
      if (this.rewards.containsKey(player.getUniqueId())) {
         EditorData editorData = this.rewards.get(player.getUniqueId());
         editorData.getRewardList().remove(reward);
         this.rewards.put(player.getUniqueId(), editorData);
      }
   }
}
