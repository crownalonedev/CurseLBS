package org.minecurse.lootbags.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.menus.LootBagPreviewMenu;
import org.minecurse.lootbags.struct.CrateType;
import org.minecurse.lootbags.struct.HalfType;
import org.minecurse.lootbags.struct.LootBag;
import org.minecurse.lootbags.utils.LootBagUtils;

public class LootBagListener implements Listener {
   private final LootBagPlugin plugin;

   public LootBagListener(LootBagPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      LootBagPlugin.getInstance().getAnimationManager().getAnimationHashMap().remove(event.getPlayer().getUniqueId());
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onInteract(PlayerInteractEvent event) {
      if (!event.getAction().equals(Action.PHYSICAL)) {
         Player player = event.getPlayer();
         ItemStack hand = player.getItemInHand();
         if (LootBagUtils.isLootBag(hand)) {
            LootBag lootBag = LootBagUtils.getLootBag(hand);
            if (lootBag != null) {
               event.setCancelled(true);
               if (this.plugin.getHypeManager().getOpeningHypeBoxes().contains(player.getUniqueId())) {
                  player.sendMessage(LootBagPlugin.hypePrefix("You cannot open a hype box while your rolling one!"));
               } else if (event.getAction().name().contains("LEFT")) {
                  if (lootBag.getType() != CrateType.MONTHLY) {
                     new LootBagPreviewMenu(lootBag).show(player);
                  }
               } else if (lootBag.getRewards().isEmpty()) {
                  player.sendMessage(LootBagPlugin.prefix("&cThis lootbag has no rewards."));
               } else if (player.getInventory().firstEmpty() == -1) {
                  player.sendMessage(LootBagPlugin.prefix("&cPlease clear a slot to open this lootbag."));
               } else {
                  double chance = 0.0;
                  if (player.hasPermission("group.infused")) {
                     chance += 7.5;
                  }

                  if (player.hasPermission("group.galactic")) {
                     chance += 5.0;
                  }

                  if (lootBag.getType() == CrateType.LOOTBAG && RandomUtil.getChance(chance)) {
                     PlayerUtils.playSound(player, Sound.LEVEL_UP, 0.75F);
                     player.sendMessage(LootBagPlugin.prefix("&7You have kept your &fLoot Bag&7 because of your &aRank&7 buff!"));
                     lootBag.executeLootBag(player, false);
                  } else {
                     lootBag.executeLootBag(player, true);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onApply(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      if (event.getClickedInventory() == player.getInventory()) {
         ItemStack cursor = event.getCursor();
         ItemStack clicked = event.getCurrentItem();
         if (event.isLeftClick() && LootBagUtils.isHalf(clicked) && LootBagUtils.isHalf(cursor)) {
            HalfType cursorHalf = LootBagUtils.getHalfType(cursor);
            HalfType clickedHalf = LootBagUtils.getHalfType(clicked);
            event.setCancelled(true);
            if (cursor.getAmount() > 1 || clicked.getAmount() > 1) {
               player.sendMessage(LootBagPlugin.prefix("&cYou can only apply 1 shard at a time"));
               PlayerUtils.playSound(player, Sound.ITEM_BREAK, 1.25F);
            } else if (LootBagUtils.getHalfLootBox(cursor) != LootBagUtils.getHalfLootBox(clicked)) {
               player.sendMessage(LootBagPlugin.prefix("&cYou must have the same lootbag shards to merge them!"));
               PlayerUtils.playSound(player, Sound.ITEM_BREAK, 1.25F);
            } else if (clickedHalf == cursorHalf) {
               player.sendMessage(LootBagPlugin.prefix("&cYou must have 2 different shards inorder to merge them!"));
               PlayerUtils.playSound(player, Sound.ITEM_BREAK, 1.25F);
            } else {
               LootBag lootBag = LootBagUtils.getHalfLootBox(clicked);
               if (lootBag != null) {
                  event.setCursor(lootBag.getLootBag());
                  event.setCurrentItem(null);
                  PlayerUtils.playSound(player, Sound.HORSE_ARMOR, 2.5F);
                  player.sendMessage(LootBagPlugin.prefix("You have &asuccessfully &7merged two shards into a {0}&7!", lootBag.getDisplayName()));
               }
            }
         }
      }
   }

   @EventHandler(ignoreCancelled = true)
   public void onEntityInteract(PlayerInteractAtEntityEvent event) {
      if (event.getRightClicked() instanceof ArmorStand) {
         event.setCancelled(true);
      }
   }

   @EventHandler(ignoreCancelled = true)
   public void onEntityInteract(PlayerInteractEntityEvent event) {
      if (event.getRightClicked() instanceof ArmorStand) {
         event.setCancelled(true);
      }
   }
}
