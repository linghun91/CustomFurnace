package cn.i7mc.customfurnace.utils;

import cn.i7mc.customfurnace.CustomFurnace;
import cn.i7mc.customfurnace.models.CustomFurnaceData;
import java.awt.Color;
import java.util.UUID;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;

public class TextDisplayUtil {
    private final CustomFurnace plugin;

    public TextDisplayUtil(CustomFurnace plugin) {
        this.plugin = plugin;
    }

    public UUID createOrUpdateFurnaceDisplay(Block block, CustomFurnaceData data, int cookTime, int cookTimeTotal) {
        if (data.getHologramUUID() != null) {
            block.getWorld().getEntities().stream().filter(entity -> entity instanceof TextDisplay && entity.getUniqueId().equals(data.getHologramUUID())).forEach(Entity::remove);
        } else {
            block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.9, 0.5), 1.0, 1.0, 1.0).stream().filter(entity -> entity instanceof TextDisplay).forEach(Entity::remove);
        }
        if (!this.plugin.getConfigManager().isArmorstandHologramEnabled()) {
            return null;
        }
        String customName = this.plugin.getConfigManager().getFurnacesConfig().getString("furnaces." + data.getLevel().getType() + ".display_name." + data.getLevel().getLevel());
        if (customName == null) {
            customName = this.plugin.getLangManager().getRawMessage("furnace.name");
        }
        String hoverDisplay = this.plugin.getLangManager().colorize(this.plugin.getLangManager().getRawMessage("furnace.hologram_hud").replace("%display%", customName).replace("%level%", String.valueOf(data.getLevel().getLevel())).replace("%payment_type%", data.getPaymentType().equals("vault") ? "\u91d1\u5e01" : "\u70b9\u5238").replace("%speed%", String.valueOf(data.getLevel().getCookingTime())));
        TextDisplay hologram = (TextDisplay)block.getWorld().spawnEntity(block.getLocation().add(0.5, this.plugin.getConfigManager().getTextDisplayYOffset(), 0.5), EntityType.TEXT_DISPLAY);
        StringBuilder textContent = new StringBuilder(hoverDisplay);
        if (cookTimeTotal > 0 && cookTime > 0) {
            String progressBar;
            String infoMsg;
            String progressActiveMsg = this.plugin.getLangManager().getRawMessage("hologram.progress_active");
            if (progressActiveMsg != null && !progressActiveMsg.isEmpty()) {
                textContent.append("\n").append(this.plugin.getLangManager().colorize(progressActiveMsg));
            }
            if ((infoMsg = this.plugin.getLangManager().getRawMessage("hologram.info")) != null && !infoMsg.isEmpty()) {
                double percentage = (double)cookTime / (double)cookTimeTotal * 100.0;
                String percentageFormatted = String.format("%.1f%%", percentage);
                String infoLine = this.plugin.getLangManager().colorize(infoMsg.replace("%percentage%", percentageFormatted).replace("%amount%", "1"));
                textContent.append("\n").append(infoLine);
            }
            if (!(progressBar = this.generateProgressBar(cookTime, cookTimeTotal)).isEmpty()) {
                textContent.append("\n").append(this.plugin.getLangManager().colorize(progressBar));
            }
        }
        hologram.setText(textContent.toString());
        this.applyTextDisplaySettings(hologram);
        return hologram.getUniqueId();
    }

    public void applyTextDisplaySettings(TextDisplay hologram) {
        String alignmentStr = this.plugin.getConfigManager().getTextDisplayAlignment();
        try {
            TextDisplay.TextAlignment alignment = TextDisplay.TextAlignment.valueOf((String)alignmentStr);
            hologram.setAlignment(alignment);
        } catch (IllegalArgumentException e) {
            this.plugin.getLogger().warning("\u65e0\u6548\u7684\u6587\u672c\u5bf9\u9f50\u65b9\u5f0f: " + alignmentStr + "\uff0c\u4f7f\u7528\u9ed8\u8ba4CENTER");
            hologram.setAlignment(TextDisplay.TextAlignment.CENTER);
        }
        hologram.setSeeThrough(this.plugin.getConfigManager().isTextDisplaySeeThrough());
        hologram.setShadowed(this.plugin.getConfigManager().isTextDisplayShadowed());
        String billboardStr = this.plugin.getConfigManager().getTextDisplayBillboard();
        try {
            Display.Billboard billboard = Display.Billboard.valueOf((String)billboardStr);
            hologram.setBillboard(billboard);
        } catch (IllegalArgumentException e) {
            this.plugin.getLogger().warning("\u65e0\u6548\u7684\u5e7f\u544a\u724c\u6a21\u5f0f: " + billboardStr + "\uff0c\u4f7f\u7528\u9ed8\u8ba4CENTER");
            hologram.setBillboard(Display.Billboard.CENTER);
        }
        int[] colorArray = this.plugin.getConfigManager().getTextDisplayBackgroundColor();
        org.bukkit.Color backgroundColor = org.bukkit.Color.fromARGB((int)colorArray[0], (int)colorArray[1], (int)colorArray[2], (int)colorArray[3]);
        hologram.setBackgroundColor(backgroundColor);
        byte opacity = this.plugin.getConfigManager().getTextDisplayOpacity();
        if (opacity >= 0) {
            hologram.setTextOpacity(opacity);
        }
        hologram.setLineWidth(this.plugin.getConfigManager().getTextDisplayLineWidth());
    }

    public String generateProgressBar(int current, int total) {
        int i;
        if (!this.plugin.getConfigManager().isProgressBarEnabled()) {
            return "";
        }
        int barLength = this.plugin.getConfigManager().getProgressBarLength();
        String character = this.plugin.getConfigManager().getProgressBarCharacter();
        String startChar = this.plugin.getConfigManager().getProgressBarStartChar();
        String endChar = this.plugin.getConfigManager().getProgressBarEndChar();
        String borderColor = this.plugin.getConfigManager().getProgressBarBorderColor();
        String emptyColor = this.plugin.getConfigManager().getProgressBarEmptyColor();
        String[] filledColors = this.plugin.getConfigManager().getProgressBarFilledColors();
        double ratio = (double)current / (double)total;
        int filledLength = (int)Math.ceil(ratio * (double)barLength);
        if (filledLength > barLength) {
            filledLength = barLength;
        }
        String filledColor = this.getProgressColor(ratio, filledColors);
        StringBuilder result = new StringBuilder();
        result.append(borderColor).append(startChar);
        for (i = 0; i < filledLength; ++i) {
            result.append(filledColor).append(character);
        }
        for (i = filledLength; i < barLength; ++i) {
            result.append(emptyColor).append(character);
        }
        result.append(borderColor).append(endChar);
        return result.toString();
    }

    private String getProgressColor(double progress, String[] colors) {
        if (colors.length == 0) {
            return "&f";
        }
        if (colors.length == 1) {
            return colors[0];
        }
        double segmentLength = 1.0 / (double)(colors.length - 1);
        int startColorIndex = (int)Math.min(Math.floor(progress / segmentLength), (double)(colors.length - 2));
        String startColorCode = colors[startColorIndex];
        String endColorCode = colors[startColorIndex + 1];
        Color startColor = this.parseMinecraftColor(startColorCode);
        Color endColor = this.parseMinecraftColor(endColorCode);
        double segmentRatio = (progress - (double)startColorIndex * segmentLength) / segmentLength;
        int r = (int)((double)startColor.getRed() + segmentRatio * (double)(endColor.getRed() - startColor.getRed()));
        int g = (int)((double)startColor.getGreen() + segmentRatio * (double)(endColor.getGreen() - startColor.getGreen()));
        int b = (int)((double)startColor.getBlue() + segmentRatio * (double)(endColor.getBlue() - startColor.getBlue()));
        return this.getClosestMinecraftColor(new Color(r, g, b));
    }

    private Color parseMinecraftColor(String colorCode) {
        if (colorCode == null || colorCode.length() < 2) {
            return Color.WHITE;
        }
        char code = colorCode.charAt(1);
        switch (code) {
            case '0': {
                return new Color(0, 0, 0);
            }
            case '1': {
                return new Color(0, 0, 170);
            }
            case '2': {
                return new Color(0, 170, 0);
            }
            case '3': {
                return new Color(0, 170, 170);
            }
            case '4': {
                return new Color(170, 0, 0);
            }
            case '5': {
                return new Color(170, 0, 170);
            }
            case '6': {
                return new Color(255, 170, 0);
            }
            case '7': {
                return new Color(170, 170, 170);
            }
            case '8': {
                return new Color(85, 85, 85);
            }
            case '9': {
                return new Color(85, 85, 255);
            }
            case 'a': {
                return new Color(85, 255, 85);
            }
            case 'b': {
                return new Color(85, 255, 255);
            }
            case 'c': {
                return new Color(255, 85, 85);
            }
            case 'd': {
                return new Color(255, 85, 255);
            }
            case 'e': {
                return new Color(255, 255, 85);
            }
            case 'f': {
                return new Color(255, 255, 255);
            }
        }
        return Color.WHITE;
    }

    private String getClosestMinecraftColor(Color color) {
        String[] codes = new String[]{"&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f"};
        Color[] colors = new Color[]{new Color(0, 0, 0), new Color(0, 0, 170), new Color(0, 170, 0), new Color(0, 170, 170), new Color(170, 0, 0), new Color(170, 0, 170), new Color(255, 170, 0), new Color(170, 170, 170), new Color(85, 85, 85), new Color(85, 85, 255), new Color(85, 255, 85), new Color(85, 255, 255), new Color(255, 85, 85), new Color(255, 85, 255), new Color(255, 255, 85), new Color(255, 255, 255)};
        int minDistance = Integer.MAX_VALUE;
        int closestIndex = 0;
        for (int i = 0; i < colors.length; ++i) {
            int distance = this.colorDistance(color, colors[i]);
            if (distance >= minDistance) continue;
            minDistance = distance;
            closestIndex = i;
        }
        return codes[closestIndex];
    }

    private int colorDistance(Color c1, Color c2) {
        int rDiff = c1.getRed() - c2.getRed();
        int gDiff = c1.getGreen() - c2.getGreen();
        int bDiff = c1.getBlue() - c2.getBlue();
        return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
    }

    public boolean updateProgressBar(Block block, UUID hologramUUID, int cookTime, int cookTimeTotal) {
        if (hologramUUID == null || !this.plugin.getConfigManager().isProgressBarEnabled()) {
            return false;
        }
        for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation().add(0.5, this.plugin.getConfigManager().getTextDisplayYOffset(), 0.5), 1.0, 1.0, 1.0)) {
            String progressBar;
            String infoMsg;
            String basicInfo;
            if (!(entity instanceof TextDisplay) || !entity.getUniqueId().equals(hologramUUID)) continue;
            TextDisplay textDisplay = (TextDisplay)entity;
            String currentText = textDisplay.getText();
            String[] lines = currentText.split("\n");
            String string = basicInfo = lines.length > 0 ? lines[0] : "";
            if (cookTimeTotal <= 0 || cookTime <= 0) {
                textDisplay.setText(basicInfo);
                return true;
            }
            StringBuilder newText = new StringBuilder(basicInfo);
            String progressActiveMsg = this.plugin.getLangManager().getRawMessage("hologram.progress_active");
            if (progressActiveMsg != null && !progressActiveMsg.isEmpty()) {
                newText.append("\n").append(this.plugin.getLangManager().colorize(progressActiveMsg));
            }
            if ((infoMsg = this.plugin.getLangManager().getRawMessage("hologram.info")) != null && !infoMsg.isEmpty()) {
                double percentage = (double)cookTime / (double)cookTimeTotal * 100.0;
                String percentageFormatted = String.format("%.1f%%", percentage);
                String infoLine = this.plugin.getLangManager().colorize(infoMsg.replace("%percentage%", percentageFormatted).replace("%amount%", "1"));
                newText.append("\n").append(infoLine);
            }
            if (!(progressBar = this.generateProgressBar(cookTime, cookTimeTotal)).isEmpty()) {
                newText.append("\n").append(this.plugin.getLangManager().colorize(progressBar));
            }
            textDisplay.setText(newText.toString());
            return true;
        }
        return false;
    }
}

