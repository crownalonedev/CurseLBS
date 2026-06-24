package org.minecurse.lootbags.manager;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.minecurse.lootbags.struct.CrateAnimation;

public class AnimationManager {
   private final HashMap<UUID, CrateAnimation> animationHashMap = new HashMap<>();

   public void addPlayer(Player player, CrateAnimation crateAnimation) {
      this.animationHashMap.put(player.getUniqueId(), crateAnimation);
   }

   public HashMap<UUID, CrateAnimation> getAnimationHashMap() {
      return this.animationHashMap;
   }
}
