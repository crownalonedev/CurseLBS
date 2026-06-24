package org.minecurse.lootbags.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.minecurse.lootbags.menus.hype.HypeBox;
import org.minecurse.lootmanager.struct.Reward;

public class HypeBoxFinishEvent extends Event {
   private static final HandlerList HANDLERS_LIST = new HandlerList();
   private final Player player;
   private final HypeBox hypeBox;
   private final Reward item;

   public HypeBoxFinishEvent(Player player, HypeBox hypeBox, Reward item) {
      this.player = player;
      this.hypeBox = hypeBox;
      this.item = item;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS_LIST;
   }

   public HandlerList getHandlers() {
      return HANDLERS_LIST;
   }

   public void call() {
      Bukkit.getServer().getPluginManager().callEvent(this);
   }

   public Player getPlayer() {
      return this.player;
   }

   public HypeBox getHypeBox() {
      return this.hypeBox;
   }

   public Reward getItem() {
      return this.item;
   }
}
