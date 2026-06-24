package org.minecurse.lootbags.manager;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;
import org.minecurse.lootbags.LootBagPlugin;
import org.minecurse.lootbags.struct.LootBag;
import org.yaml.snakeyaml.Yaml;

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

         new File(this.getPath() + "/lootbags/" + bag.getInternalName() + ".yml").delete();
         new File(this.getPath() + "/lootbags/" + bag.getInternalName() + ".yaml").delete();
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
         ObjectMapper jsonMapper = new ObjectMapper();
         jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
         jsonMapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));

         this.renameJsonToYml(dir);

         for (File f : Objects.requireNonNull(dir.listFiles())) {
            String name = f.getName().toLowerCase();
            if (name.endsWith(".yml") || name.endsWith(".yaml")) {
               try {
                  LootBag loaded = this.readYaml(f, jsonMapper);
                  if (loaded != null) {
                     this.LootBags.add(loaded);
                  }
               } catch (Exception var10) {
                  this.plugin.getLogger().warning("Failed to parse " + f.getName() + " normally, attempting repair: " + var10.toString());
                  try {
                     LootBag salvaged = this.repairAndLoad(f, jsonMapper);
                     if (salvaged != null) {
                        this.LootBags.add(salvaged);
                     }
                  } catch (Exception var9) {
                     this.plugin.getLogger().severe("Could not repair " + f.getName() + ": " + var9.toString());
                  }
               }
            }
         }

         this.LootBags.sort(Comparator.comparing(LootBag::getInternalName));
         this.originalDump = Lists.newArrayList(this.LootBags);
      } else {
         this.originalDump = Lists.newArrayList();
      }
   }

   private void renameJsonToYml(File dir) {
      for (File f : Objects.requireNonNull(dir.listFiles())) {
         if (f.getName().toLowerCase().endsWith(".json")) {
            String ymlName = f.getName().substring(0, f.getName().length() - 5) + ".yml";
            File ymlFile = new File(dir, ymlName);
            if (ymlFile.exists()) {
               f.delete();
            } else {
               if (f.renameTo(ymlFile)) {
                  this.plugin.getLogger().info("Renamed " + f.getName() + " to " + ymlName);
               } else {
                  this.plugin.getLogger().warning("Could not rename " + f.getName() + " to .yml - will try loading it as-is.");
               }
            }
         }
      }
   }

   public void saveToDisk() {
      File dir = new File(this.path + "/lootbags");
      dir.mkdir();
      ObjectMapper jsonMapper = new ObjectMapper();
      jsonMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
      jsonMapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));

      if (this.originalDump == null) {
         this.originalDump = Lists.newArrayList(this.LootBags);
      }

      for (LootBag LootBag2 : this.originalDump) {
         if (!this.LootBags.contains(LootBag2)) {
            new File(this.path + "/lootbags/" + LootBag2.getInternalName() + ".yml").delete();
            new File(this.path + "/lootbags/" + LootBag2.getInternalName() + ".json").delete();
         } else {
            File ymlFile = new File(this.path + "/lootbags/" + LootBag2.getInternalName() + ".yml");
            File oldJson = new File(this.path + "/lootbags/" + LootBag2.getInternalName() + ".json");

            try {
               this.writeYaml(ymlFile, LootBag2, jsonMapper);
               if (oldJson.exists()) {
                  oldJson.delete();
               }
            } catch (Exception var6) {
               this.plugin.getLogger().warning("Failed to save lootbag " + LootBag2.getInternalName() + ": " + var6.toString());
            }
         }
      }

      this.originalDump = Lists.newArrayList(this.LootBags);
   }

   @SuppressWarnings("unchecked")
   private void writeYaml(File file, LootBag bag, ObjectMapper jsonMapper) throws IOException {
      StringBuilder sb = new StringBuilder();
      String name = bag.getInternalName() != null ? bag.getInternalName() : "lootbag";

      sb.append("# ═══════════════════════════════════════════════════════════════\n");
      sb.append("# Lootbag Configuration: ").append(name).append("\n");
      sb.append("# ═══════════════════════════════════════════════════════════════\n");
      sb.append("# Edit this file and run /el reload to apply changes.\n");
      sb.append("\n");

      sb.append("# ── Identity ───────────────────────────────────────────────────\n");
      sb.append("# Internal name used in commands (A-Z, 0-9, _ only)\n");
      sb.append("internalName: ").append(yamlQuote(bag.getInternalName())).append("\n");
      sb.append("\n");

      sb.append("# Display name shown in-game (supports & color codes)\n");
      sb.append("displayName: ").append(yamlQuote(bag.getDisplayNameField())).append("\n");
      sb.append("\n");

      sb.append("# ── Appearance ─────────────────────────────────────────────────\n");
      sb.append("# Material of the lootbag item (e.g. CHEST, SKULL_ITEM, ENDER_CHEST)\n");
      sb.append("# If a texture is set below, this is overridden by a player head\n");
      sb.append("material: ").append(yamlQuote(bag.getMaterialField())).append("\n");
      sb.append("\n");

      sb.append("# Base64 player-head texture value (from head databases)\n");
      sb.append("# Set to null to use the default material instead\n");
      sb.append("texture: ").append(bag.hasTexture() ? yamlQuote(bag.getTexture()) : "null").append("\n");
      sb.append("\n");

      sb.append("# Custom lore lines shown on the lootbag item\n");
      sb.append("# Each line supports & color codes. Use an empty list [] for no lore.\n");
      List<String> lore = bag.getLore();
      if (lore == null || lore.isEmpty()) {
         sb.append("lore: []\n");
      } else {
         sb.append("lore:\n");
         for (String line : lore) {
            sb.append("  - ").append(yamlQuote(line)).append("\n");
         }
      }
      sb.append("\n");

      sb.append("# ── Rewards ────────────────────────────────────────────────────\n");
      sb.append("# Minimum number of random rewards given per open\n");
      sb.append("minRewards: ").append(bag.getMinRewards()).append("\n");
      sb.append("\n");
      sb.append("# Maximum number of random rewards given per open\n");
      sb.append("maxRewards: ").append(bag.getMaxRewards()).append("\n");
      sb.append("\n");
      sb.append("# If true, always give exactly maxRewards (ignores minRewards)\n");
      sb.append("alwaysMax: ").append(bag.isAlwaysMax()).append("\n");
      sb.append("\n");
      sb.append("# If true, give ALL rewards instead of picking randomly\n");
      sb.append("bundle: ").append(bag.isBundle()).append("\n");
      sb.append("\n");

      sb.append("# ── Display Options ────────────────────────────────────────────\n");
      sb.append("# Apply an enchantment glow to the lootbag item\n");
      sb.append("glowing: ").append(bag.isGlowing()).append("\n");
      sb.append("\n");
      sb.append("# Broadcast a message to the server when this lootbag is opened\n");
      sb.append("broadcast: ").append(bag.isBroadcast()).append("\n");
      sb.append("\n");
      sb.append("# Show bonus rewards in the item lore\n");
      sb.append("bonusLore: ").append(bag.isBonusLore()).append("\n");
      sb.append("\n");
      sb.append("# Show the reward list in the item lore\n");
      sb.append("rewardLore: ").append(bag.isRewardLore()).append("\n");
      sb.append("\n");
      sb.append("# Feature this lootbag in the display menu (only one at a time)\n");
      sb.append("showcasedLootBag: ").append(bag.isShowcasedLootBag()).append("\n");
      sb.append("\n");

      sb.append("# ── Type & Animation ───────────────────────────────────────────\n");
      sb.append("# Lootbag type: LOOTBAG, MONTHLY, HYPE_BOX, GENERATOR, ARMOR_CACHE, BUNDLE, LUCKY_BLOCK\n");
      sb.append("type: ").append(bag.getType().name()).append("\n");
      sb.append("\n");
      sb.append("# Animation type: NORMAL, CIRCLE, GENERATOR\n");
      sb.append("animationType: ").append(bag.getAnimationType().name()).append("\n");
      sb.append("\n");

      sb.append("# ── Rewards Data ───────────────────────────────────────────────\n");
      sb.append("# Reward items are stored as serialized objects below.\n");
      sb.append("# Use the in-game /el edit menu to add/remove rewards.\n");
      sb.append("rewards:\n");
      appendRewards(sb, bag.getRewards(), jsonMapper);
      sb.append("jackpotRewards:\n");
      appendRewards(sb, bag.getJackpotRewards(), jsonMapper);
      sb.append("bonusRewards:\n");
      appendRewards(sb, bag.getBonusRewards(), jsonMapper);

      sb.append("\n");
      sb.append("# ── Internal (do not edit unless you know what you're doing) ──\n");
      sb.append("# Serialized item data (material, name, lore, enchants, etc.)\n");
      sb.append("# The fields above override this where applicable.\n");
      sb.append("item: ").append(bag.getItemField() != null ? yamlQuote(bag.getItemField()) : "null").append("\n");

      if (file.exists()) {
         file.delete();
      }

      Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
   }

   @SuppressWarnings("unchecked")
   private void appendRewards(StringBuilder sb, List<?> rewards, ObjectMapper jsonMapper) {
      if (rewards == null || rewards.isEmpty()) {
         sb.append("  []\n");
      } else {
         boolean anyWritten = false;
         for (Object reward : rewards) {
            try {
               Object mapped = jsonMapper.convertValue(reward, Object.class);
               String json = jsonMapper.writeValueAsString(mapped);
               sb.append("  - ").append(yamlQuote(json)).append("\n");
               anyWritten = true;
            } catch (Exception var8) {
               sb.append("  - '").append("INVALID_REWARD_SKIPPED").append("'\n");
               this.plugin.getLogger().warning("Skipped a reward that could not be serialized: " + var8.getMessage());
            }
         }

         if (!anyWritten) {
            sb.append("  []\n");
         }
      }
   }

   private String yamlQuote(String value) {
      if (value == null) {
         return "null";
      }

      if (value.isEmpty()) {
         return "\"\"";
      }

      if (value.contains("\n") || value.contains("\r")) {
         String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
         return "\"" + escaped + "\"";
      }

      if (value.matches("[0-9].*") || value.contains(":") || value.contains("#") || value.contains("{")
            || value.contains("}") || value.contains("[") || value.contains("]") || value.contains(",")
            || value.contains("&") || value.contains("*") || value.contains("?") || value.contains("|")
            || value.contains(">") || value.contains("%") || value.contains("@") || value.contains("`")
            || value.contains("+") || value.contains("/") || value.contains("=")
            || value.startsWith("-") || value.startsWith("!") || value.startsWith("'") || value.startsWith("\"")) {
         return "'" + value.replace("'", "''") + "'";
      }

      return value;
   }

   private LootBag readYaml(File file, ObjectMapper jsonMapper) throws IOException {
      String yamlStr = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
      Yaml yaml = new Yaml();
      Object loaded = yaml.load(yamlStr);
      if (!(loaded instanceof Map)) {
         return null;
      } else {
         @SuppressWarnings("unchecked")
         Map<String, Object> data = (Map<String, Object>) loaded;
         return jsonMapper.convertValue(data, LootBag.class);
      }
   }

   @SuppressWarnings("unchecked")
   private LootBag repairAndLoad(File file, ObjectMapper jsonMapper) throws IOException {
      String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
      String repaired = this.repairMultilineValues(content);

      LootBag bag = null;
      try {
         Yaml yaml = new Yaml();
         Object loaded = yaml.load(repaired);
         if (loaded instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) loaded;
            bag = jsonMapper.convertValue(data, LootBag.class);
         }
      } catch (Exception var8) {
         this.plugin.getLogger().warning("Repair attempt for " + file.getName() + " still failed: " + var8.getMessage());
      }

      if (bag != null) {
         try {
            this.writeYaml(file, bag, jsonMapper);
            this.plugin.getLogger().info("Repaired and re-saved " + file.getName());
         } catch (Exception var7) {
            this.plugin.getLogger().warning("Loaded " + file.getName() + " but could not re-save it: " + var7.getMessage());
         }
      }

      return bag;
   }

   private String repairMultilineValues(String content) {
      String[] lines = content.split("\n", -1);
      StringBuilder out = new StringBuilder();
      String currentListKey = null;

      for (int i = 0; i < lines.length; i++) {
         String line = lines[i];
         String trimmed = line.replaceAll("\r$", "");
         String stripped = trimmed.trim();

         if (stripped.isEmpty() || stripped.startsWith("#")) {
            out.append(trimmed).append("\n");
            continue;
         }

         if (stripped.startsWith("- ")) {
            if (currentListKey != null) {
               out.append(trimmed).append("\n");
            } else {
               out.append(trimmed).append("\n");
            }
            continue;
         }

         int colonIdx = this.findKeyColon(stripped);
         if (colonIdx > 0) {
            String key = stripped.substring(0, colonIdx).trim();
            String value = (colonIdx + 1 < stripped.length()) ? stripped.substring(colonIdx + 1).trim() : "";
            currentListKey = null;

            if (value.isEmpty() || value.equals("[]")) {
               if (this.isListKey(key)) {
                  currentListKey = key;
               }
               out.append(trimmed).append("\n");
               continue;
            }

            if ((key.equals("item") || key.equals("texture") || key.equals("displayName") || key.equals("material"))
                  && (value.startsWith("'") || value.startsWith("\"") || this.looksLikeBase64Start(value))) {
               StringBuilder fullValue = new StringBuilder(this.stripYamlQuotes(value));
               int j = i + 1;
               while (j < lines.length) {
                  String nextTrimmed = lines[j].replaceAll("\r$", "");
                  String nextStripped = nextTrimmed.trim();
                  if (nextStripped.isEmpty() || nextStripped.startsWith("#") || nextStripped.startsWith("- ")) {
                     break;
                  }

                  int nextColon = this.findKeyColon(nextStripped);
                  if (nextColon > 0 && this.isKnownKey(nextStripped.substring(0, nextColon).trim())) {
                     break;
                  }

                  if (nextColon > 0 && this.isListKey(nextStripped.substring(0, nextColon).trim())) {
                     break;
                  }

                  fullValue.append(nextStripped);
                  j++;
               }

               out.append(key).append(": ").append(this.yamlQuote(fullValue.toString())).append("\n");
               i = j - 1;
               continue;
            }
         }

         out.append(trimmed).append("\n");
      }

      return out.toString();
   }

   private int findKeyColon(String line) {
      boolean inSingleQuote = false;
      boolean inDoubleQuote = false;

      for (int i = 0; i < line.length(); i++) {
         char c = line.charAt(i);
         if (c == '\'' && !inDoubleQuote) {
            inSingleQuote = !inSingleQuote;
         } else if (c == '"' && !inSingleQuote) {
            inDoubleQuote = !inDoubleQuote;
         } else if (c == ':' && !inSingleQuote && !inDoubleQuote) {
            if (i + 1 >= line.length() || line.charAt(i + 1) == ' ' || i + 1 == line.length()) {
               return i;
            }
         }
      }

      return -1;
   }

   private boolean looksLikeBase64Start(String value) {
      if (value.isEmpty()) {
         return false;
      }

      if (value.startsWith("'") || value.startsWith("\"")) {
         String inner = value.substring(1);
         return !inner.isEmpty() && inner.matches("[A-Za-z0-9+/=].*");
      } else {
         return value.matches("[A-Za-z0-9+/=]+.*") && value.length() > 20;
      }
   }

   private boolean isListKey(String key) {
      return key.equals("lore") || key.equals("rewards") || key.equals("jackpotRewards") || key.equals("bonusRewards");
   }

   private boolean isKnownKey(String key) {
      return key.equals("internalName") || key.equals("displayName") || key.equals("material")
            || key.equals("texture") || key.equals("lore") || key.equals("rewards")
            || key.equals("jackpotRewards") || key.equals("bonusRewards") || key.equals("minRewards")
            || key.equals("maxRewards") || key.equals("broadcast") || key.equals("bundle")
            || key.equals("glowing") || key.equals("bonusLore") || key.equals("rewardLore")
            || key.equals("alwaysMax") || key.equals("showcasedLootBag") || key.equals("animationType")
            || key.equals("type") || key.equals("item");
   }

   private String stripYamlQuotes(String value) {
      if (value == null || value.isEmpty() || value.equals("null") || value.equals("[]")) {
         return value;
      }

      if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
         return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
      }

      if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
         return value.substring(1, value.length() - 1).replace("''", "'");
      }

      return value;
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
