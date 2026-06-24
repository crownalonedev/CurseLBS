package org.minecurse.lootbags.battle;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.minecurse.inventorypets.utils.SkullUtils;
import org.minecurse.inventorypets.utils.SkullUtils.Type;
import org.minecurse.lootbags.LootBagPlugin;

public class BattleManager {
   private final List<BattleInfo> activeBattles = new ArrayList<>();
   private final ItemStack botHead = SkullUtils.fromUrl(
      Type.ITEM, "http://textures.minecraft.net/texture/739481c453bda4f9f594521c50569b0585a6edbb2182475a5e3e23a582a7a13"
   );

   public List<BattleInfo> getActiveBattles() {
      return this.activeBattles;
   }

   public ItemStack getBotHead() {
      return this.botHead;
   }

   public static BattleManager getInstance() {
      return LootBagPlugin.getInstance().getBattleManager();
   }
}
