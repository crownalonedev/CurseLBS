package org.minecurse.lootbags.menus.rewards;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.chest.ChestMenu;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.lootbags.struct.LootBag;
import org.minecurse.lootmanager.struct.Reward;

public class RewardConfirmMenu {
   private final LootBag table;
   private final Reward reward;
   private final ChestMenu inventory;

   public RewardConfirmMenu(LootBag table, Reward reward) {
      this.table = table;
      this.reward = reward;
      this.inventory = new ChestMenu("Confirm Item Deletion", 3);
   }

   public void show(Player player) {
      ItemBuilder confirm = new ItemBuilder(Material.STAINED_GLASS);
      ItemBuilder deny = new ItemBuilder(Material.STAINED_GLASS);
      confirm.durability(5);
      confirm.name("&a&lConfirm Item Deletion");
      confirm.lore("");
      confirm.lore("&7Click here to &aconfirm &7the process.");
      deny.durability(14);
      deny.name("&c&lCancel Item Deletion");
      deny.lore("");
      deny.lore("&7Click here to &ccancel &7the process.");
      this.inventory.fillSides(Button.PLACEHOLDER);
      RewardMenu menu = new RewardMenu(this.table);
      this.inventory.setButton(11, new Button(confirm, (player1, clickInformation) -> {
         this.table.getRewards().remove(this.reward);
         this.table.getBonusRewards().remove(this.reward);
         this.table.getJackpotRewards().remove(this.reward);
         menu.show(player);
      }));
      this.inventory.setButton(13, new ItemStack(this.reward.getItemStack()));
      this.inventory.setButton(15, new Button(deny, (player1, clickInformation) -> menu.show(player)));
      PlayerUtils.playSound(player, Sound.ANVIL_USE, 1.5F);
      this.inventory.buildInventory();
      this.inventory.show(player);
   }

   public LootBag getTable() {
      return this.table;
   }

   public Reward getReward() {
      return this.reward;
   }

   public ChestMenu getInventory() {
      return this.inventory;
   }
}
