package org.minecurse.lootbags.api;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.minecurse.commons.utils.StringUtil;

public final class LootBagNotificationApi {
   private static final Set<UUID> FILTERED_LOOTBAG_BROADCASTS = Collections.newSetFromMap(new ConcurrentHashMap<>());

   private LootBagNotificationApi() {
   }

   public static void addFiltered(Player player) {
      if (player != null) {
         addFiltered(player.getUniqueId());
      }
   }

   public static void filterLootBagBroadcasts(Player player) {
      addFiltered(player);
   }

   public static void addFiltered(UUID uuid) {
      if (uuid != null) {
         FILTERED_LOOTBAG_BROADCASTS.add(uuid);
      }
   }

   public static void filterLootBagBroadcasts(UUID uuid) {
      addFiltered(uuid);
   }

   public static void removeFiltered(Player player) {
      if (player != null) {
         removeFiltered(player.getUniqueId());
      }
   }

   public static void unfilterLootBagBroadcasts(Player player) {
      removeFiltered(player);
   }

   public static void removeFiltered(UUID uuid) {
      if (uuid != null) {
         FILTERED_LOOTBAG_BROADCASTS.remove(uuid);
      }
   }

   public static void unfilterLootBagBroadcasts(UUID uuid) {
      removeFiltered(uuid);
   }

   public static boolean isFiltered(Player player) {
      return player != null && isFiltered(player.getUniqueId());
   }

   public static boolean isFilteringLootBagBroadcasts(Player player) {
      return isFiltered(player);
   }

   public static boolean isFiltered(UUID uuid) {
      return uuid != null && FILTERED_LOOTBAG_BROADCASTS.contains(uuid);
   }

   public static boolean isFilteringLootBagBroadcasts(UUID uuid) {
      return isFiltered(uuid);
   }

   public static void clearFiltered() {
      FILTERED_LOOTBAG_BROADCASTS.clear();
   }

   public static void sendLootBagBroadcast(String message) {
      sendLootBagBroadcast(message, null);
   }

   public static void sendLootBagBroadcast(String message, Player opener) {
      String coloredMessage = StringUtil.color(message);

      for (Player player : Bukkit.getOnlinePlayers()) {
         if (shouldReceiveLootBagBroadcast(player, opener)) {
            player.sendMessage(coloredMessage);
         }
      }
   }

   public static boolean shouldReceiveLootBagBroadcast(Player player, Player opener) {
      return player != null && (opener != null && player.getUniqueId().equals(opener.getUniqueId()) || !isFiltered(player));
   }
}
