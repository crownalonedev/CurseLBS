package org.minecurse.lootbags.struct;

import java.util.List;
import org.minecurse.lootmanager.struct.Reward;

public class EditorData {
   private final LootBag lootBag;
   private final List<Reward> rewardList;

   public EditorData(LootBag lootBag, List<Reward> rewardList) {
      this.lootBag = lootBag;
      this.rewardList = rewardList;
   }

   public LootBag getLootBag() {
      return this.lootBag;
   }

   public List<Reward> getRewardList() {
      return this.rewardList;
   }
}
