package org.minecurse.lootbags.menus.rewards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.paginated.PaginatedMenu;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.menus.LootbagListMenu;
import org.minecurse.lootbags.struct.LootBag;
import org.minecurse.lootmanager.struct.Reward;
import org.minecurse.lootmanager.struct.RewardData;
import org.minecurse.lootmanager.struct.RewardDataType;
import org.minecurse.lootmanager.utils.RewardUtils;

public class RewardMenu implements Listener {
   private final LootBag lootBag;
   private final Set<UUID> viewing = new HashSet<>();
   private final HashMap<UUID, Reward> typing = new HashMap<>();
   private final List<String> validActions = new ArrayList<>();
   private PaginatedMenu inventory;

   public RewardMenu(LootBag lootBag) {
      this.validActions.add("setchance");
      this.validActions.add("setminamount");
      this.validActions.add("setmaxamount");
      this.validActions.add("setbonus");
      this.validActions.add("setcost");
      this.validActions.add("setpullamount");
      this.validActions.add("setlootboxdata");
      this.lootBag = lootBag;
      Bukkit.getServer().getPluginManager().registerEvents(this, LootBagPlugin.getInstance());
   }

   public void show(Player player) {
      ArrayList<Reward> all = new ArrayList<>(this.lootBag.getRewards());
      List<Reward> vanishedSomehow = this.lootBag.getBonusRewards().stream().filter(reward -> !all.contains(reward)).collect(Collectors.toList());
      all.addAll(vanishedSomehow);
      this.inventory = new PaginatedMenu("Editing Lootbag Rewards: " + this.lootBag.getInternalName(), 6, 28);
      ItemBuilder des = new ItemBuilder(Material.EMPTY_MAP).name("&2&lLoot Bag Information");
      des.lore("");
      des.lore("&aHow do you use edit the Loot Bag Rewards?");
      des.lore("&7&l* &fLeft click to give your self a item.");
      des.lore("&7&l* &fShift Left click to delete a item.");
      des.lore("&7&l* &fRight click to edit a reward items properties.");
      des.lore("&7&l* &fShift Right click to change the item rarity.");
      des.lore("");
      des.lore("&7Preview the Item by hovering over it.");
      this.inventory.fillSides(Button.PLACEHOLDER);

      for (Reward reward2 : all) {
         ItemBuilder builder = new ItemBuilder(reward2.getItemStack().clone());
         builder.lore("");
         builder.lore("&a&lRelative Chance");
         double abs = RewardUtils.getPercentageCount(this.lootBag.getRewards()) != 0.0
            ? reward2.getChance() / RewardUtils.getPercentageCount(this.lootBag.getRewards())
            : 0.0;
         builder.lore("&a" + reward2.getChance() + "%");
         builder.lore("");
         builder.lore("&e&lAbsolute Chance");
         builder.lore("&e" + abs * 100.0 + "%");
         builder.lore("");
         builder.lore("&B&lRarity");
         builder.lore(reward2.getRarity().getDisplayName());
         builder.lore("");
         builder.lore("&c&lMinimum Amount");
         builder.lore("&c" + reward2.getMin());
         builder.lore("");
         builder.lore("&6&lMaximum Amount");
         builder.lore("&6" + reward2.getMax());
         builder.lore("");
         builder.lore("&d&lReward Type");
         builder.lore("&f" + (this.lootBag.getBonusRewards().contains(reward2) ? "Bonus" : "Regular"));
         builder.lore("");
         builder.lore("&a&lMax Pulls");
         builder.lore("&f" + reward2.getMaxPulls());
         builder.lore("");
         builder.lore("&b&lReward Value");
         builder.lore("&f" + reward2.getCost());
         builder.lore("");
         builder.lore("&c&lReward Data &7(Lootbox Only)");
         builder.lore("&f" + (reward2.getData() != null ? reward2.getData().displayTheString() : "&cNo Data"));
         builder.lore("");
         builder.lore("&7Click to edit this reward.");
         this.inventory
            .addButton(
               new Button(
                  builder,
                  (player1, clickInformation) -> {
                     if (clickInformation.getClickType().isShiftClick() && clickInformation.getClickType().isLeftClick()) {
                        this.viewing.remove(player.getUniqueId());
                        new RewardConfirmMenu(this.lootBag, reward2).show(player);
                     } else if (clickInformation.getClickType().isShiftClick() && clickInformation.getClickType().isRightClick()) {
                        this.viewing.remove(player.getUniqueId());
                        RewardRarityMenu rarityMenu = new RewardRarityMenu(this.lootBag, reward2);
                        rarityMenu.show(player);
                     } else if (clickInformation.getClickType().isLeftClick() && !clickInformation.getClickType().isShiftClick()) {
                        this.viewing.remove(player.getUniqueId());
                        player.getInventory().addItem(new ItemStack[]{reward2.getItemStack()});
                     } else if (clickInformation.getClickType().isRightClick() && !clickInformation.getClickType().isShiftClick()) {
                        this.viewing.remove(player.getUniqueId());
                        player.closeInventory();
                        this.typing.put(player.getUniqueId(), reward2);
                        player.sendMessage(LootBagPlugin.prefix("Type a new action for this reward."));
                        player.sendMessage(
                           LootBagPlugin.prefix(
                              "Valid Actions: &6setchance (chance) | setcost (cost) | setbonus (true:false) | setminamount (amount) | setmaxamount (amount) | setpullamount (amount) | setlootboxdata (type) (color)"
                           )
                        );
                     }
                  }
               )
            );
      }

      this.inventory
         .setButton(
            49,
            new Button(
               new ItemBuilder(Material.ARROW).name("&bGo Back").lore("&7Go back to select").lore("&7a new lootbag."),
               (player1, clickInformation) -> new LootbagListMenu(this.lootBag.getType()).show(player1)
            )
         );
      this.inventory.buildInventory();
      this.inventory.show(player);
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
   public void onClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      if (this.getInventory() != null && this.getInventory().getTitle() != null && event.getInventory().getTitle().equals(this.getInventory().getTitle())) {
         ItemStack item = event.getCurrentItem();
         if (item != null && item.getType() != Material.AIR && event.getClickedInventory() != event.getInventory() && event.isLeftClick()) {
            event.setCancelled(true);
            LootBag lootBag = this.getLootBag();
            lootBag.addReward(item);
            if (this.getLootBag().isAlwaysMax()) {
               lootBag.setMaxRewards(lootBag.getRewards().size());
               lootBag.setMinRewards(lootBag.getRewards().size());
            }

            player.sendMessage(LootBagPlugin.prefix("You have added a new item called {0}&7.", item.getItemMeta().getDisplayName()));
            this.show(player);
         }
      }
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
   public void onChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      if (this.typing.containsKey(player.getUniqueId())) {
         event.setCancelled(true);
         String message = event.getMessage();
         String[] splitMessage = message.split(" ");
         if (splitMessage.length < 2) {
            player.sendMessage(LootBagPlugin.prefix("&cInvalid command format."));
            player.sendMessage(LootBagPlugin.prefix("Usage: <command> <argument>"));
         } else {
            String checker = splitMessage[0];
            String argument = splitMessage[1];
            if (!this.validActions.contains(checker)) {
               player.sendMessage(LootBagPlugin.prefix("&cThis is not a valid action."));
               player.sendMessage(LootBagPlugin.prefix("Valid Actions: " + this.validActions));
            } else if (!message.contains("setbonus")) {
               if (message.contains("setpullamount")) {
                  try {
                     int amount = Integer.parseInt(argument);
                     Reward reward2 = this.typing.get(player.getUniqueId());
                     reward2.setMaxPulls(amount);
                     this.handleAction(player);
                  } catch (Exception var15) {
                     player.sendMessage(LootBagPlugin.prefix("&cInvalid amount."));
                     return;
                  }
               }

               if (message.contains("setcost")) {
                  try {
                     int amount = Integer.parseInt(argument);
                     Reward reward2 = this.typing.get(player.getUniqueId());
                     reward2.setCost(amount);
                     this.handleAction(player);
                  } catch (Exception var14) {
                     player.sendMessage(LootBagPlugin.prefix("&cInvalid amount."));
                     return;
                  }
               }

               if (message.contains("setlootboxdata")) {
                  try {
                     RewardData data = new RewardData(RewardDataType.valueOf(argument));
                     ChatColor color = ChatColor.getByChar(splitMessage[2].charAt(0));
                     data.setChatColor(color);
                     Reward reward = this.typing.get(player.getUniqueId());
                     reward.setData(data);
                     this.handleAction(player);
                  } catch (Exception var13) {
                     player.sendMessage(LootBagPlugin.prefix("&cInvalid Data Type or Chat Color!"));
                     player.sendMessage(
                        LootBagPlugin.prefix(
                           "&cData Types: "
                              + Arrays.stream(RewardDataType.values()).<CharSequence>map(RewardDataType::getDisplay).collect(Collectors.joining(", "))
                        )
                     );
                     player.sendMessage(LootBagPlugin.prefix("&cProvide a number for the color code!"));
                     return;
                  }
               }

               if (message.contains("setmaxamount")) {
                  try {
                     int amount = Integer.parseInt(argument);
                     Reward reward2 = this.typing.get(player.getUniqueId());
                     reward2.setMax(amount);
                     this.handleAction(player);
                  } catch (Exception var12) {
                     player.sendMessage(LootBagPlugin.prefix("&cInvalid amount."));
                     return;
                  }
               }

               if (message.contains("setminamount")) {
                  try {
                     int amount = Integer.parseInt(argument);
                     Reward reward2 = this.typing.get(player.getUniqueId());
                     reward2.setMin(amount);
                     this.handleAction(player);
                  } catch (Exception var11) {
                     player.sendMessage(LootBagPlugin.prefix("&cInvalid amount."));
                     return;
                  }
               }

               if (message.contains("setchance")) {
                  try {
                     double chance = Double.parseDouble(argument);
                     Reward reward = this.typing.get(player.getUniqueId());
                     reward.setChance(chance);
                     this.handleAction(player);
                  } catch (Exception var10) {
                     player.sendMessage(LootBagPlugin.prefix("&cInvalid chance."));
                  }
               }
            } else {
               try {
                  boolean parse = Boolean.parseBoolean(argument);
                  Reward reward3 = this.typing.get(player.getUniqueId());
                  if (this.lootBag.getBonusRewards().contains(reward3) && parse) {
                     player.sendMessage(LootBagPlugin.prefix("&cThis reward is already a bonus item."));
                     this.typing.remove(player.getUniqueId());
                  } else if (this.lootBag.getBonusRewards().contains(reward3) && !parse) {
                     player.sendMessage(LootBagPlugin.prefix("&cRemoving bonus reward!"));
                     this.lootBag.getBonusRewards().remove(reward3);
                     this.handleAction(player);
                  } else {
                     player.sendMessage(LootBagPlugin.prefix("&cAdding bonus reward!"));
                     this.lootBag.getBonusRewards().add(reward3);
                     this.handleAction(player);
                  }
               } catch (Exception var16) {
                  player.sendMessage(LootBagPlugin.prefix("&cInvalid Boolean."));
               }
            }
         }
      }
   }

   private void handleAction(Player player) {
      this.typing.remove(player.getUniqueId());
      this.show(player);
      player.sendMessage(LootBagPlugin.prefix("Your current lootbag action has been completed."));
      PlayerUtils.playSound(player, Sound.LEVEL_UP, 0.75F);
   }

   public LootBag getLootBag() {
      return this.lootBag;
   }

   public PaginatedMenu getInventory() {
      return this.inventory;
   }
}
