package org.minecurse.lootbags.animation;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;
import org.minecurse.commons.particle.ParticleEffect;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.api.LootBagNotificationApi;
import org.minecurse.lootbags.settings.Settings;
import org.minecurse.lootbags.struct.CrateAnimation;
import org.minecurse.lootmanager.struct.Reward;
import org.minecurse.lootmanager.utils.RewardUtils;
import org.minecurse.modules.utils.ItemUtil;

public class RevolvingCircleTask extends BukkitRunnable {
   private static final int TICKS_TO_STOP_ROTATING = 200;
   private final CrateAnimation crate;
   private final List<Hologram> holograms;
   private final Map<Hologram, String[]> spinningHolograms;
   private final List<Reward> rewardsToGive;
   private final double radius;
   private Hologram centerHologram;
   private double currentAngle = 0.0;
   private int tickCounter = 0;
   private int armorIndex = 0;
   private boolean rotating = true;
   private boolean completeRotating = false;
   private float glassBreak = 0.45F;

   public RevolvingCircleTask(CrateAnimation crate) {
      this.crate = crate;
      this.holograms = new ArrayList<>();
      this.spinningHolograms = new ConcurrentHashMap<>();
      this.rewardsToGive = new ArrayList<>();
      int calculated = (int)Math.round(crate.getAmountToGive() * 1.25 / Math.PI);
      this.radius = Math.max(3, calculated);
      Bukkit.getLogger().info("[LootBags DEBUG] Creating animation: amountToGive=" + crate.getAmountToGive() + " radius=" + this.radius);
      this.centerHologram = this.createCenterHologram();
      this.createSpinningHolograms();
   }

   private void createSpinningHolograms() {
      List<Reward> regular = this.crate
         .getLootBag()
         .getRewards()
         .stream()
         .filter(r -> !this.crate.getLootBag().getBonusRewards().contains(r))
         .collect(Collectors.toList());
      int count = this.crate.getAmountToGive();
      double angleStep = (Math.PI * 2) / Math.max(count, 1);
      Bukkit.getLogger().info("[LootBags DEBUG] Spawning " + count + " spinning holograms, regular pool size=" + regular.size());

      for (int i = 0; i < count; i++) {
         String holoName = "lootbag_spin_" + UUID.randomUUID().toString().substring(0, 8);
         double angle = i * angleStep;
         double x = this.crate.getCenter().getX() + this.radius * Math.cos(angle);
         double y = this.crate.getCenter().getY() + 1.0;
         double z = this.crate.getCenter().getZ() + this.radius * Math.sin(angle);
         Location spawnLoc = new Location(this.crate.getCenter().getWorld(), x, y, z);
         Reward sample = this.safeGetReward(regular);
         String displayName = sample != null ? ItemUtil.getDisplayName(sample.getItemStack()) : "&6???";
         String material = sample != null ? sample.getItemStack().getType().name() : "CHEST";
         Hologram hologram = DHAPI.createHologram(holoName, spawnLoc, false, Arrays.asList(StringUtil.color(displayName), "#ICON: " + material));
         Bukkit.getLogger()
            .info(
               "[LootBags DEBUG] Created spin holo #"
                  + i
                  + " name="
                  + holoName
                  + " at "
                  + String.format("%.1f %.1f %.1f", x, y, z)
                  + " displayName="
                  + displayName
                  + " material="
                  + material
                  + " disabled="
                  + hologram.isDisabled()
            );
         this.spinningHolograms.put(hologram, new String[]{"text", "icon"});
         this.holograms.add(hologram);
      }
   }

   private Hologram createCenterHologram() {
      String holoName = "lootbag_center_" + UUID.randomUUID().toString().substring(0, 8);
      List<Reward> bonus = this.crate.getLootBag().getBonusRewards();
      Reward sample = this.safeGetReward(bonus);
      String material = sample != null ? sample.getItemStack().getType().name() : "CHEST";
      Hologram hologram = DHAPI.createHologram(holoName, this.crate.getCenter(), false, Arrays.asList("#ICON: " + material));
      Bukkit.getLogger().info("[LootBags DEBUG] Created center holo name=" + holoName + " material=" + material + " disabled=" + hologram.isDisabled());
      return hologram;
   }

   private Reward safeGetReward(List<Reward> list) {
      if (list != null && !list.isEmpty()) {
         if (list.size() == 1) {
            return list.get(0);
         }

         try {
            return RewardUtils.getRandomReward(list);
         } catch (Exception e) {
            Bukkit.getLogger().warning("[LootBags] RewardUtils.getRandomReward failed (" + e.getMessage() + "), using list.get(0)");
            return list.get(0);
         }
      } else {
         return null;
      }
   }

   public void run() {
      this.tickCounter++;
      if (this.crate.getPlayer() != null && this.crate.getPlayer().isOnline()) {
         if (this.tickCounter % 20 == 0) {
            for (int i = 0; i < 3; i++) {
               ParticleEffect.FIREWORKS_SPARK
                  .display(this.crate.getCenter(), ThreadLocalRandom.current().nextInt(0, 2), 1.0F, ThreadLocalRandom.current().nextInt(0, 2), 1.0F, 10);
            }
         }

         if (this.rotating) {
            this.holograms.removeIf(h -> h == null || h.isDisabled());
            if (this.tickCounter % 2 == 0) {
               this.glassBreak = (float)(this.glassBreak + 0.01);
               this.crate.getPlayer().playSound(this.crate.getCenter(), Sound.CHICKEN_EGG_POP, 1.0F, this.glassBreak);
            }

            if (this.tickCounter == 1) {
               Bukkit.getLogger().info("[LootBags DEBUG] Tick 1 – spinning holo count after removeIf: " + this.holograms.size());
            }

            double angleStep = this.holograms.isEmpty() ? 0.0 : (Math.PI * 2) / this.holograms.size();

            for (int i = 0; i < this.holograms.size(); i++) {
               Hologram hologram = this.holograms.get(i);
               double angle = this.currentAngle + i * angleStep;
               double x = this.crate.getCenter().getX() + this.radius * Math.cos(angle);
               double y = this.crate.getCenter().getY() + 1.0;
               double z = this.crate.getCenter().getZ() + this.radius * Math.sin(angle);
               DHAPI.moveHologram(hologram, new Location(this.crate.getCenter().getWorld(), x, y, z));
            }

            if (this.tickCounter % 10 == 0) {
               List<Reward> regular = this.crate
                  .getLootBag()
                  .getRewards()
                  .stream()
                  .filter(r -> !this.crate.getLootBag().getBonusRewards().contains(r))
                  .collect(Collectors.toList());

               for (Hologram hologram : this.holograms) {
                  Reward reward = this.safeGetReward(regular);
                  if (reward != null) {
                     if (reward.getMaxPulls() != -1) {
                        int safety = 0;

                        while (reward.getTimesPulled() >= reward.getMaxPulls() && safety++ < 20) {
                           reward = this.safeGetReward(regular);
                           if (reward == null) {
                              break;
                           }
                        }
                     }

                     if (reward != null) {
                        DHAPI.setHologramLine(hologram, 0, StringUtil.color(ItemUtil.getDisplayName(reward.getItemStack())));
                        ItemStack item = reward.getItemStack().clone();
                        item.setAmount(1);
                        DHAPI.setHologramLine(hologram, 1, "#ICON: " + item.getType().name());
                        if (this.completeRotating) {
                           this.rewardsToGive.add(reward);
                        }
                     }
                  }
               }

               List<Reward> jackpot = this.crate.getLootBag().getBonusRewards();
               if (!jackpot.isEmpty()) {
                  Reward jackpotReward = this.safeGetReward(jackpot);
                  if (jackpotReward != null) {
                     if (jackpotReward.getMaxPulls() != -1) {
                        int safety = 0;

                        while (jackpotReward.getTimesPulled() == jackpotReward.getMaxPulls() && safety++ < 20) {
                           jackpotReward = this.safeGetReward(jackpot);
                           if (jackpotReward == null) {
                              break;
                           }
                        }
                     }

                     if (jackpotReward != null) {
                        this.crate.updateHologram(ItemUtil.getDisplayName(jackpotReward.getItemStack()));
                        ItemStack jItem = jackpotReward.getItemStack().clone();
                        jItem.setAmount(1);
                        if (this.centerHologram != null && this.centerHologram.getPage(0) != null && !this.centerHologram.getPage(0).getLines().isEmpty()) {
                           DHAPI.setHologramLine(this.centerHologram, 0, "#ICON: " + jItem.getType().name());
                        }

                        if (this.completeRotating) {
                           this.rewardsToGive.add(jackpotReward);
                        }
                     }
                  }
               }
            }

            if (this.tickCounter >= 140) {
               this.currentAngle += 0.05;
            } else {
               this.currentAngle += 0.1;
            }

            if (this.currentAngle > Math.PI * 2) {
               this.currentAngle -= Math.PI * 2;
            }

            if (this.tickCounter == 199) {
               this.completeRotating = true;
            }

            if (this.tickCounter >= 200) {
               this.rotating = false;
            }
         } else if (this.centerHologram == null && this.holograms.isEmpty()) {
            this.crate.killAnimation();
         } else {
            if (this.tickCounter % 15 == 0 && !this.holograms.isEmpty()) {
               if (this.armorIndex >= this.holograms.size()) {
                  this.armorIndex = 0;
               }

               Hologram h = this.holograms.get(this.armorIndex);
               if (h != null) {
                  h.getLocation().getWorld().strikeLightningEffect(h.getLocation());
                  h.delete();
                  this.holograms.remove(h);
               }

               if (this.armorIndex >= this.holograms.size()) {
                  this.armorIndex = 0;
               }
            }

            if (this.holograms.isEmpty() && this.tickCounter % 15 != 0 && this.centerHologram != null) {
               this.centerHologram.getLocation().getWorld().strikeLightningEffect(this.centerHologram.getLocation());
               if (this.rewardsToGive.isEmpty()) {
                  this.centerHologram.delete();
                  this.centerHologram = null;
                  LootBagPlugin.getInstance().getAnimationManager().getAnimationHashMap().remove(this.crate.getPlayer().getUniqueId());
                  return;
               }

               int last = this.rewardsToGive.size() - 1;
               Reward reward2 = this.rewardsToGive.get(last);
               if (reward2.getMaxPulls() != -1) {
                  if (reward2.getTimesPulled() <= reward2.getMaxPulls()) {
                     reward2.setTimesPulled(reward2.getTimesPulled() + 1);
                  } else {
                     int safety = 0;

                     while (reward2.getMaxPulls() == reward2.getTimesPulled() && safety++ < 20) {
                        Reward next = this.safeGetReward(this.crate.getLootBag().getBonusRewards());
                        if (next == null) {
                           break;
                        }

                        reward2 = next;
                     }
                  }
               }

               this.centerHologram.delete();
               this.centerHologram = null;
               LootBagPlugin.getInstance().getAnimationManager().getAnimationHashMap().remove(this.crate.getPlayer().getUniqueId());
               if (this.crate.getLootBag().isBroadcast()) {
                  this.sendLootboxBroadcast(reward2);
               }

               this.rewardsToGive.forEach(r -> r.handleGive(this.crate.getPlayer()));
            }
         }
      } else {
         this.crate.killAnimation();
      }
   }

   public void sendLootboxBroadcast(Reward reward) {
      for (Player all : Bukkit.getOnlinePlayers()) {
         if (LootBagNotificationApi.shouldReceiveLootBagBroadcast(all, this.crate.getPlayer())) {
            all.sendMessage(StringUtil.color("&5&l&m-----&d&l&m-----&5&l&m-----&d&l&m-----&5&l&m-----&d&l&m-----&5&l&m-----"));
            all.sendMessage(StringUtil.center(this.crate.getLootBag().getDisplayName()));
            all.sendMessage(StringUtil.center(StringUtil.color("&7► &d" + Settings.storeUrl + "&7◄")));
            all.sendMessage("");
            all.sendMessage(StringUtil.color("&6" + this.crate.getPlayer().getName() + " &fhas opened the following rewards:"));
            all.sendMessage("");
            all.sendMessage(StringUtil.color("&6&lLEGENDARY"));
            this.rewardsToGive
               .stream()
               .limit(this.rewardsToGive.size() - 1)
               .forEach(r -> all.sendMessage(StringUtil.color("&e&l• &f&l" + r.getMax() + "x " + ItemUtil.getDisplayName(r.getItemStack()))));
            all.sendMessage("");
            all.sendMessage(StringUtil.color("&4&lSUPERIOR"));
            all.sendMessage(StringUtil.color("&4&l• &f&l" + reward.getMax() + "x " + ItemUtil.getDisplayName(reward.getItemStack())));
            all.sendMessage("");
            all.sendMessage(StringUtil.color("&5&l&m-----&d&l&m-----&5&l&m-----&d&l&m-----&5&l&m-----&d&l&m-----&5&l&m-----"));
         }
      }
   }

   public void sendLootboxTitle(Player player, Reward reward) {
      player.sendTitle(
         new Title(
            StringUtil.color(this.crate.getLootBag().getDisplayName()),
            StringUtil.color("&2You have won &f&l" + reward.getMax() + "x&r " + ItemUtil.getDisplayName(reward.getItemStack())),
            40,
            40,
            40
         )
      );
   }

   public CrateAnimation getCrate() {
      return this.crate;
   }

   public List<Hologram> getHolograms() {
      return this.holograms;
   }

   public Map<Hologram, String[]> getSpinningHolograms() {
      return this.spinningHolograms;
   }

   public List<Reward> getRewardsToGive() {
      return this.rewardsToGive;
   }

   public Hologram getCenterHologram() {
      return this.centerHologram;
   }

   public double getCurrentAngle() {
      return this.currentAngle;
   }

   public int getTickCounter() {
      return this.tickCounter;
   }

   public int getArmorIndex() {
      return this.armorIndex;
   }

   public boolean isRotating() {
      return this.rotating;
   }

   public boolean isCompleteRotating() {
      return this.completeRotating;
   }

   public float getGlassBreak() {
      return this.glassBreak;
   }

   public double getRadius() {
      return this.radius;
   }
}
