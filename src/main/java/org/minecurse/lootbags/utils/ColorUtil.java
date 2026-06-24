package org.minecurse.lootbags.utils;

import org.bukkit.ChatColor;

public class ColorUtil {
   public static final String Green = ChatColor.GREEN.toString();
   public static final String Red = ChatColor.RED.toString();
   public static final String Blue = ChatColor.BLUE.toString();
   public static final String Yellow = ChatColor.YELLOW.toString();
   public static final String Gold = ChatColor.GOLD.toString();
   public static final String White = ChatColor.WHITE.toString();
   public static final String Gray = ChatColor.GRAY.toString();
   public static final String DarkGray = ChatColor.DARK_GRAY.toString();
   public static final String Aqua = ChatColor.AQUA.toString();
   public static final String DarkAqua = ChatColor.DARK_AQUA.toString();
   public static final String DarkGreen = ChatColor.DARK_GREEN.toString();
   public static final String DarkRed = ChatColor.DARK_RED.toString();
   public static final String DarkBlue = ChatColor.DARK_BLUE.toString();
   public static final String DarkPurple = ChatColor.DARK_PURPLE.toString();
   public static final String LightPurple = ChatColor.LIGHT_PURPLE.toString();
   public static final String Black = ChatColor.BLACK.toString();
   public static final String Bold = ChatColor.BOLD.toString();
   public static final String Italic = ChatColor.ITALIC.toString();
   public static final String Underline = ChatColor.UNDERLINE.toString();
   public static final String Strikethrough = ChatColor.STRIKETHROUGH.toString();
   public static final String Reset = ChatColor.RESET.toString();

   public static String translate(String text) {
      return ChatColor.translateAlternateColorCodes('&', text);
   }
}
