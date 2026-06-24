package org.minecurse.lootbags.battle;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.minecurse.lootmanager.struct.Reward;

public class PlayerBattle {
   private Player player;
   private boolean isBot;
   private List<Reward> itemsUnboxed = new ArrayList<>();
   private double currentValue = 0.0;

   public Player getPlayer() {
      return this.player;
   }

   public void setPlayer(Player player) {
      this.player = player;
   }

   public List<Reward> getItemsUnboxed() {
      return this.itemsUnboxed;
   }

   public void setItemsUnboxed(List<Reward> itemsUnboxed) {
      this.itemsUnboxed = itemsUnboxed;
   }

   public void setCurrentValue(double currentValue) {
      this.currentValue = currentValue;
   }

   public double getCurrentValue() {
      return this.currentValue;
   }

   public PlayerBattle(Player player, boolean isBot) {
      this.player = player;
      this.isBot = isBot;
   }

   public boolean isBot() {
      return this.isBot;
   }

   public void setBot(boolean isBot) {
      this.isBot = isBot;
   }

   public void addValue(double val) {
      this.currentValue += val;
   }
}
