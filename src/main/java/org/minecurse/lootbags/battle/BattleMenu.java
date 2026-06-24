package org.minecurse.lootbags.battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecurse.commons.CommonsPlugin;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.commons.utils.inventory.ClickableItem;
import org.minecurse.commons.utils.inventory.SmartInventory;
import org.minecurse.commons.utils.inventory.content.InventoryContents;
import org.minecurse.commons.utils.inventory.content.InventoryProvider;
import org.minecurse.inventorypets.utils.SkullUtils;
import org.minecurse.inventorypets.utils.SkullUtils.Type;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.settings.Settings;
import org.minecurse.lootbags.utils.DropUtils;
import org.minecurse.lootmanager.struct.Reward;
import org.minecurse.lootmanager.utils.RewardUtils;

public class BattleMenu extends BukkitRunnable implements InventoryProvider {
   private static final ItemBuilder SIDES = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name("&7");
   private final BattleInfo battleInfo;
   private final int tickEndPoint;
   private final Map<PlayerBattle, List<Reward>> currentItems = new HashMap<>();
   private final SmartInventory inventory;
   private int currentRollIndex = 0;
   private int currentCount = 0;
   private float pitch;
   private PlayerBattle winner = null;
   private int property = 0;
   private boolean hasGivenOne = false;
   private boolean hasGivenTwo = false;

   public BattleInfo getBattleInfo() {
      return this.battleInfo;
   }

   public int getTickEndPoint() {
      return this.tickEndPoint;
   }

   public Map<PlayerBattle, List<Reward>> getCurrentItems() {
      return this.currentItems;
   }

   public SmartInventory getInventory() {
      return this.inventory;
   }

   public int getCurrentRollIndex() {
      return this.currentRollIndex;
   }

   public int getCurrentCount() {
      return this.currentCount;
   }

   public float getPitch() {
      return this.pitch;
   }

   public PlayerBattle getWinner() {
      return this.winner;
   }

   public int getProperty() {
      return this.property;
   }

   public boolean isHasGivenOne() {
      return this.hasGivenOne;
   }

   public boolean isHasGivenTwo() {
      return this.hasGivenTwo;
   }

   public BattleMenu(BattleInfo battleInfo) {
      this.battleInfo = battleInfo;
      this.tickEndPoint = 46;
      this.inventory = SmartInventory.builder()
         .id("hype-box")
         .provider(this)
         .size(6, 9)
         .title(StringUtil.color(battleInfo.getLootBag().getDisplayName() + "&8 - Hype Box Battle"))
         .closeable(false)
         .clickable(false)
         .build();
      this.currentItems.put(battleInfo.getPlayerOne(), this.rollRewards());
      this.currentItems.put(battleInfo.getPlayerTwo(), this.rollRewards());
      this.pitch = 1.1F;
      this.runTaskTimer(LootBagPlugin.getInstance(), 0L, 1L);
   }

   private void queueNext() {
      this.currentItems.replace(this.battleInfo.getPlayerOne(), this.rollRewards());
      this.currentItems.replace(this.battleInfo.getPlayerTwo(), this.rollRewards());
      this.currentCount++;
      if (this.currentCount != this.battleInfo.getCount()) {
         this.currentRollIndex = 0;
         this.pitch = 1.1F;
      } else {
         this.inventory.setCloseable(true);
         this.cancel();
         String loser;
         if (this.battleInfo.getPlayerOne().getCurrentValue() > this.battleInfo.getPlayerTwo().getCurrentValue()) {
            this.winner = this.battleInfo.getPlayerOne();
            loser = this.battleInfo.getPlayerTwo().isBot() ? Settings.botName : this.battleInfo.getPlayerTwo().getPlayer().getDisplayName();
         } else if (this.battleInfo.getPlayerOne().getCurrentValue() < this.battleInfo.getPlayerTwo().getCurrentValue()) {
            this.winner = this.battleInfo.getPlayerTwo();
            loser = this.battleInfo.getPlayerOne().isBot() ? Settings.botName : this.battleInfo.getPlayerOne().getPlayer().getDisplayName();
         } else if (RandomUtil.getChance(50.0)) {
            this.winner = this.battleInfo.getPlayerOne();
            loser = this.battleInfo.getPlayerTwo().isBot() ? Settings.botName : this.battleInfo.getPlayerTwo().getPlayer().getDisplayName();
         } else {
            this.winner = this.battleInfo.getPlayerTwo();
            loser = this.battleInfo.getPlayerOne().isBot() ? Settings.botName : this.battleInfo.getPlayerOne().getPlayer().getDisplayName();
         }

         String winnerName = this.winner.isBot() ? Settings.botName : this.winner.getPlayer().getDisplayName();
         List<Reward> won = new ArrayList<>();
         won.addAll(this.battleInfo.getPlayerOne().getItemsUnboxed());
         won.addAll(this.battleInfo.getPlayerTwo().getItemsUnboxed());
         if (!this.winner.isBot()) {
            won.forEach(reward -> {
               PlayerUtils.playSound(this.winner.getPlayer(), Sound.CHICKEN_EGG_POP, 1.0F);
               DropUtils.giveOrDropProtectedItem(this.winner.getPlayer(), reward.getItemStack());
            });
         }

         for (Player all : Bukkit.getOnlinePlayers()) {
            all.sendMessage("");
            all.sendMessage(
               LootBagPlugin.hypePrefix(
                  winnerName
                     + " &fhas defeated "
                     + loser
                     + " &fin a "
                     + this.getBattleInfo().getLootBag().getDisplayName()
                     + "&r &fbattle and received the following... &a&lW &for &C&LL&f?"
               )
            );

            for (Reward items : won) {
               all.sendMessage(StringUtil.color("&f&l • " + items.getItemStack().getAmount() + "x " + items.getItemStack().getItemMeta().getDisplayName()));
            }

            all.sendMessage("");
         }

         BattleManager.getInstance().getActiveBattles().remove(this.battleInfo);
      }
   }

   public void init(Player player, InventoryContents contents) {
      contents.fillRow(1, ClickableItem.empty(SIDES));
      contents.fillRow(3, ClickableItem.empty(SIDES));

      for (int i = 0; i < 6; i++) {
         contents.set(i, 4, ClickableItem.empty(SIDES));
      }

      ItemBuilder p1 = new ItemBuilder(
         this.battleInfo.getPlayerOne().isBot()
            ? BattleManager.getInstance().getBotHead()
            : SkullUtils.fromName(Type.ITEM, this.battleInfo.getPlayerOne().getPlayer().getName())
      );
      ItemBuilder p2 = new ItemBuilder(
         this.battleInfo.getPlayerTwo().isBot()
            ? BattleManager.getInstance().getBotHead()
            : SkullUtils.fromName(Type.ITEM, this.battleInfo.getPlayerTwo().getPlayer().getName())
      );
      p1.name((this.battleInfo.getPlayerOne().isBot() ? Settings.botName : this.battleInfo.getPlayerOne().getPlayer().getDisplayName()) + "&7's Statistics");
      p1.lore(new String[]{"", "&d&lRewards Value", "&f&L • &2$&a" + this.battleInfo.getPlayerOne().getCurrentValue()});
      p2.name((this.battleInfo.getPlayerTwo().isBot() ? Settings.botName : this.battleInfo.getPlayerTwo().getPlayer().getDisplayName()) + "&7's Statistics");
      p2.lore(new String[]{"", "&d&lRewards Value", "&f&L • &2$&a" + this.battleInfo.getPlayerTwo().getCurrentValue()});
      ItemBuilder itemBuilder1 = new ItemBuilder(Material.HOPPER).name("&fReward Below:");
      contents.set(0, 2, ClickableItem.empty(p1));
      contents.set(0, 6, ClickableItem.empty(p2));
      contents.set(1, 2, ClickableItem.empty(itemBuilder1));
      contents.set(1, 6, ClickableItem.empty(itemBuilder1));
      ItemBuilder builder = new ItemBuilder(this.battleInfo.getLootBag().getLootBag());
      builder.setAmount(this.battleInfo.getCount() - this.currentCount);
      contents.set(1, 4, ClickableItem.empty(builder));

      for (int n = 0; n < 4; n++) {
         int itemIndex = (this.currentItems.get(this.battleInfo.getPlayerOne()).size() - 1 - n + this.currentRollIndex)
            % this.currentItems.get(this.battleInfo.getPlayerOne()).size();
         contents.set(2, n, ClickableItem.empty(this.currentItems.get(this.battleInfo.getPlayerOne()).get(itemIndex).getItemStack()));
         if (this.currentRollIndex >= this.tickEndPoint && n == 3 && !this.hasGivenOne) {
            Reward reward = this.currentItems.get(this.battleInfo.getPlayerOne()).get(itemIndex);
            this.battleInfo.getPlayerOne().getItemsUnboxed().add(reward);
            this.battleInfo.getPlayerOne().addValue(reward.getCost());
            this.hasGivenOne = true;
         } else {
            Reward reward = this.currentItems.get(this.battleInfo.getPlayerOne()).get(itemIndex);
            ItemBuilder item = new ItemBuilder(reward.getItemStack().clone()).lore(new String[]{"", "&fPrice: &b$" + reward.getCost()});
            contents.set(2, n, ClickableItem.empty(item));
         }
      }

      for (int m = 0; m < 4; m++) {
         int itemIndex = (this.currentItems.get(this.battleInfo.getPlayerTwo()).size() - 1 - m + this.currentRollIndex - 1)
            % this.currentItems.get(this.battleInfo.getPlayerTwo()).size();
         Reward reward = this.currentItems.get(this.battleInfo.getPlayerTwo()).get(itemIndex);
         if (this.currentRollIndex >= this.tickEndPoint && m == 2 && !this.hasGivenTwo) {
            this.battleInfo.getPlayerTwo().getItemsUnboxed().add(reward);
            this.battleInfo.getPlayerTwo().addValue(reward.getCost());
            this.hasGivenTwo = true;
         }

         ItemBuilder item = new ItemBuilder(reward.getItemStack().clone()).lore(new String[]{"", "&fPrice: &b$" + reward.getCost()});
         contents.set(2, 5 + m, ClickableItem.empty(item));
      }

      for (int k = 0; k < this.battleInfo.getPlayerOne().getItemsUnboxed().size(); k++) {
         contents.set(
            k > 3 ? 5 : 4,
            k > 3 ? k - 4 : k,
            ClickableItem.empty(
               new ItemBuilder(this.battleInfo.getPlayerOne().getItemsUnboxed().get(k).getItemStack().clone())
                  .lore(new String[]{"", "&fPrice: &b$" + this.battleInfo.getPlayerOne().getItemsUnboxed().get(k).getCost()})
            )
         );
      }

      for (int j = 0; j < this.battleInfo.getPlayerTwo().getItemsUnboxed().size(); j++) {
         int row = j > 3 ? 5 : 4;
         int column = j > 3 ? j + 5 - 4 : 5 + j;
         ItemBuilder item = new ItemBuilder(this.battleInfo.getPlayerTwo().getItemsUnboxed().get(j).getItemStack().clone())
            .lore(new String[]{"", "&fPrice: &b$" + this.battleInfo.getPlayerTwo().getItemsUnboxed().get(j).getCost()});
         contents.set(row, column, ClickableItem.empty(item));
      }
   }

   private List<Reward> rollRewards() {
      List<Reward> items = new ArrayList<>();

      for (int i = 0; i < 50; i++) {
         items.add(RewardUtils.getRandomReward(this.battleInfo.getLootBag().getRewards()));
      }

      return items;
   }

   public void run() {
      if (this.currentRollIndex < this.tickEndPoint) {
         int diff = this.tickEndPoint - this.currentRollIndex;
         int interval = diff >= 6 ? this.currentRollIndex / 8 + 1 : (diff >= 3 ? this.currentRollIndex / 3 + 1 : this.currentRollIndex / 2 + 1);
         int state = this.property++;
         if (state % interval == 0) {
            PlayerUtils.playSound(this.battleInfo.getPlayerOne().getPlayer(), Sound.NOTE_PLING, this.pitch);
            PlayerUtils.playSound(this.battleInfo.getPlayerTwo().getPlayer(), Sound.NOTE_PLING, this.pitch);
            this.pitch += 0.02F;
            this.currentRollIndex++;
            Optional<InventoryContents> contentsp1 = CommonsPlugin.getInstance().getInventoryManager().getContents(this.battleInfo.getPlayerOne().getPlayer());
            contentsp1.ifPresent(inventoryContents -> this.init(this.battleInfo.getPlayerOne().getPlayer(), inventoryContents));
            Optional<InventoryContents> contentsp2 = CommonsPlugin.getInstance().getInventoryManager().getContents(this.battleInfo.getPlayerTwo().getPlayer());
            contentsp2.ifPresent(inventoryContents -> this.init(this.battleInfo.getPlayerTwo().getPlayer(), inventoryContents));
            if (this.currentRollIndex >= this.tickEndPoint) {
               this.currentRollIndex = 0;
               this.hasGivenOne = false;
               this.hasGivenTwo = false;
               this.queueNext();
               PlayerUtils.playSound(this.battleInfo.getPlayerOne().getPlayer(), Sound.LEVEL_UP, 4.0F);
               PlayerUtils.playSound(this.battleInfo.getPlayerTwo().getPlayer(), Sound.LEVEL_UP, 4.0F);
               this.property = 0;
            }
         }
      }
   }
}
