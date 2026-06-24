package org.minecurse.lootbags.battle;

import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.struct.LootBag;

public class BattleInfo {
   private final LootBag lootBag;
   private final int count;
   private PlayerBattle playerOne;
   private PlayerBattle playerTwo;
   private BattleMenu battleMenu;
   private boolean isActive = false;

   public LootBag getLootBag() {
      return this.lootBag;
   }

   public int getCount() {
      return this.count;
   }

   public PlayerBattle getPlayerOne() {
      return this.playerOne;
   }

   public void setPlayerOne(PlayerBattle playerOne) {
      this.playerOne = playerOne;
   }

   public PlayerBattle getPlayerTwo() {
      return this.playerTwo;
   }

   public void setPlayerTwo(PlayerBattle playerTwo) {
      this.playerTwo = playerTwo;
   }

   public BattleMenu getBattleMenu() {
      return this.battleMenu;
   }

   public BattleInfo(PlayerBattle playerOne, PlayerBattle playerTwo, LootBag lootBag, int count) {
      this.playerOne = playerOne;
      this.playerTwo = playerTwo;
      this.lootBag = lootBag;
      this.count = count;
      BattleManager.getInstance().getActiveBattles().add(this);
   }

   public boolean isActive() {
      return this.isActive;
   }

   public void start() {
      this.isActive = true;
      this.playerOne.getPlayer().sendMessage(LootBagPlugin.hypePrefix("&aYour battle has started!"));
      this.battleMenu = new BattleMenu(this);
      this.battleMenu.getInventory().open(this.playerOne.getPlayer());
      if (!this.playerTwo.isBot()) {
         this.battleMenu.getInventory().open(this.playerTwo.getPlayer());
         this.playerTwo.getPlayer().sendMessage(LootBagPlugin.hypePrefix("&aYour battle has started!"));
      }
   }
}
