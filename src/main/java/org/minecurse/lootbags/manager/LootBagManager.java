package org.minecurse.lootbags.manager;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.struct.LootBag;

public class LootBagManager {
   private final JavaPlugin plugin;
   private final String path;
   private final String file;
   private List<LootBag> LootBags = Lists.newArrayList();
   private List<LootBag> originalDump;

   public LootBagManager(JavaPlugin plugin, String path, String file) {
      this.plugin = plugin;
      this.path = path;
      this.file = file;
      this.initialize();
   }

   public static LootBagManager getInstance() {
      return LootBagPlugin.getInstance().getManager();
   }

   public void addLootBag(LootBag LootBag2) {
      this.LootBags.add(LootBag2);
      if (this.originalDump == null) {
         this.originalDump = Lists.newArrayList();
      }

      this.originalDump.add(LootBag2);
   }

   public void initialize() {
      File file = new File(this.path);
      if (!file.exists()) {
         file.mkdir();
      }

      if (!(file = new File(this.path + "/" + this.file)).exists()) {
         try {
            file.createNewFile();
         } catch (IOException var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   public LootBag getByName(String name) {
      for (LootBag LootBag2 : this.LootBags) {
         if (LootBag2.getInternalName().equalsIgnoreCase(name)) {
            return LootBag2;
         }
      }

      return null;
   }

   public LootBag findLootBag(String name) {
      for (LootBag LootBag2 : this.LootBags) {
         if (LootBag2.getInternalName().equalsIgnoreCase(name)) {
            return LootBag2;
         }
      }

      return null;
   }

   public boolean isLootBag(String name) {
      for (LootBag LootBag2 : this.LootBags) {
         if (LootBag2.getInternalName().equalsIgnoreCase(name)) {
            return true;
         }
      }

      return false;
   }

   public void removeLootBag(LootBag bag) {
      if (bag != null) {
         this.LootBags.remove(bag);
         if (this.originalDump != null) {
            this.originalDump.remove(bag);
         }

         new File(this.getPath() + "/lootbags/" + bag.getInternalName() + ".json").delete();
      }
   }

   public LootBag findShowcasedLootBag() {
      return this.getLootBags().stream().filter(LootBag::isShowcasedLootBag).findFirst().orElse(null);
   }

   public boolean isAlreadyToggled() {
      return this.findShowcasedLootBag() != null;
   }

   public void loadFromDisk() {
      File dir = new File(this.getPath() + "/lootbags");
      this.LootBags = Lists.newArrayList();
      if (dir.exists()) {
         ObjectMapper mapper = new ObjectMapper();
         mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
         mapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));

         for (File f : Objects.requireNonNull(dir.listFiles())) {
            String name = f.getName().toLowerCase();
            if (name.endsWith(".json")) {
               try {
                  LootBag loaded = mapper.readValue(f, LootBag.class);
                  if (loaded != null) {
                     this.LootBags.add(loaded);
                  }
               } catch (Exception var9) {
                  this.plugin.getLogger().warning("Failed to load lootbag from " + f.getName() + ": " + var9.getMessage());
               }
            }
         }

         this.LootBags.sort(Comparator.comparing(LootBag::getInternalName));
         this.originalDump = Lists.newArrayList(this.LootBags);
      } else {
         this.originalDump = Lists.newArrayList();
      }
   }

   private volatile boolean dirty = false;
   private final Object saveLock = new Object();

   public void saveToDisk() {
      this.dirty = true;
   }

   public void forceSaveToDisk() {
      this.dirty = true;
      this.processSave();
   }

   public void processSave() {
      if (!this.dirty) {
         return;
      }

      synchronized (this.saveLock) {
         this.dirty = false;
         File dir = new File(this.path + "/lootbags");
         dir.mkdir();
         ObjectMapper mapper = new ObjectMapper();
         mapper.enable(SerializationFeature.INDENT_OUTPUT);
         mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
         mapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));

         if (this.originalDump == null) {
            this.originalDump = Lists.newArrayList(this.LootBags);
         }

         for (LootBag LootBag2 : this.originalDump) {
            if (!this.LootBags.contains(LootBag2)) {
               new File(this.path + "/lootbags/" + LootBag2.getInternalName() + ".json").delete();
            } else {
               File jsonFile = new File(this.path + "/lootbags/" + LootBag2.getInternalName() + ".json");
               this.writeWithRetry(jsonFile, LootBag2, mapper);
            }
         }

         this.originalDump = Lists.newArrayList(this.LootBags);
      }
   }

   private void writeWithRetry(File jsonFile, LootBag bag, ObjectMapper mapper) {
      for (int attempt = 1; attempt <= 3; attempt++) {
         try {
            mapper.writeValue(jsonFile, bag);
            return;
         } catch (Exception var8) {
            if (attempt == 3) {
               this.plugin.getLogger().warning("Failed to save lootbag " + bag.getInternalName() + " after 3 attempts: " + var8.toString());
            } else {
               try {
                  Thread.sleep(50L * attempt);
               } catch (InterruptedException ignored) {
                  Thread.currentThread().interrupt();
                  return;
               }
            }
         }
      }
   }

   public JavaPlugin getPlugin() {
      return this.plugin;
   }

   public String getPath() {
      return this.path;
   }

   public String getFile() {
      return this.file;
   }

   public List<LootBag> getLootBags() {
      return this.LootBags;
   }

   public List<LootBag> getOriginalDump() {
      return this.originalDump;
   }
}
