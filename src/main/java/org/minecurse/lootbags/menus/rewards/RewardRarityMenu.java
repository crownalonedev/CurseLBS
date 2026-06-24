package org.minecurse.lootbags.menus.rewards;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.chest.ChestMenu;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.struct.LootBag;
import org.minecurse.lootmanager.struct.Rarity;
import org.minecurse.lootmanager.struct.Reward;

public class RewardRarityMenu {
   private final LootBag table;
   private final Reward reward;
   private final ChestMenu inventory;

   public RewardRarityMenu(LootBag table, Reward reward) {
      this.table = table;
      this.reward = reward;
      this.inventory = new ChestMenu("Editing Rarity: " + table.getInternalName(), 1);
   }

   public void show(Player player) {
      RewardMenu menu = new RewardMenu(this.table);

      for (Rarity rarity : Rarity.values()) {
         ItemBuilder builder = new ItemBuilder(Material.WOOL)
            .durability(rarity.getColor().getWoolData())
            .name(rarity.getDisplayName())
            .lore("")
            .lore("&7Click here to select this rarity.");
         this.inventory.addButton(new Button(builder, (player1, clickInformation) -> {
            this.reward.setRarity(rarity);
            menu.show(player);
            player.sendMessage(LootBagPlugin.prefix("Your current lootbag action has been completed. &7(Rarity Selected: {0}&7)", rarity.getDisplayName()));
            this.sound(player);
         }));
      }

      this.inventory.buildInventory();
      this.inventory.show(player);
   }

   private void sound(Player player) {
      PlayerUtils.playSound(player, Sound.LEVEL_UP, 0.75F);
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
