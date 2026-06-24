package org.minecurse.lootbags.struct;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.minecurse.commons.utils.Logger;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.animation.RevolvingCircleTask;
import org.minecurse.lootbags.settings.Settings;

public class CrateAnimation {
   private final Player player;
   private final LootBag lootBag;
   private final Location center;
   private final Hologram hologram;
   private final int amountToGive;
   private RevolvingCircleTask task;
   private static final int JACKPOT_LINE_INDEX = 4;

   public CrateAnimation(Player player, LootBag lootBag, int amountToGive) {
      this.player = player;
      this.lootBag = lootBag;
      this.amountToGive = amountToGive;
      this.center = player.getEyeLocation().clone();
      String holoName = "lootbag_crate_" + UUID.randomUUID().toString().substring(0, 8);
      this.hologram = DHAPI.createHologram(
         holoName,
         this.center.clone().add(0.0, 1.0, 0.0),
         false,
         Arrays.asList(StringUtil.color(lootBag.getDisplayName()), "", StringUtil.color(Settings.crateHologramLine), "", StringUtil.color("&6Jackpot Reward"))
      );
      this.startAnimation();
   }

   public void updateHologram(String displayName) {
      DHAPI.setHologramLine(this.hologram, 4, StringUtil.color(displayName));
   }

   public void killAnimation() {
      this.task.getSpinningHolograms().clear();
      this.task.getHolograms().forEach(Hologram::delete);
      this.hologram.delete();
      this.task.cancel();
      Logger.log(StringUtil.format("Killing Hologram at X:{0} Y:{1} Z:{2}", new Object[]{this.center.getX(), this.center.getY(), this.center.getZ()}));
   }

   public void startAnimation() {
      this.task = new RevolvingCircleTask(this);
      this.task.runTaskTimer(LootBagPlugin.getInstance(), 0L, 1L);
   }

   public Player getPlayer() {
      return this.player;
   }

   public LootBag getLootBag() {
      return this.lootBag;
   }

   public Location getCenter() {
      return this.center;
   }

   public Hologram getHologram() {
      return this.hologram;
   }

   public RevolvingCircleTask getTask() {
      return this.task;
   }

   public int getAmountToGive() {
      return this.amountToGive;
   }
}
