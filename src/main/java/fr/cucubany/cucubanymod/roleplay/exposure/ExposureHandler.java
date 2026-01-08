package fr.cucubany.cucubanymod.roleplay.exposure;

import net.minecraft.world.entity.player.Player;

public class ExposureHandler {
    public static final String EXPOSURE_TAG = "exposureTicks";
    public static final String EXPOSURE_LEVEL_TAG = "exposureLevel";
    public static final int MAX_EXPOSURE = 100; // Valeur maximale d'exposition avant de déclencher un effet

    public static int getExposure(Player player) {
        return player.getPersistentData().getInt(EXPOSURE_TAG);
    }

    public static void incrementExposure(Player player) {
        int current = getExposure(player);
        if (current + 1 >= MAX_EXPOSURE) {
            incrementExposureLevel(player);
            resetExposure(player); // Réinitialiser l'exposition après avoir atteint le maximum
        } else {
            player.getPersistentData().putInt(EXPOSURE_TAG, current + 1);
        }
    }

    public static void resetExposure(Player player) {
        player.getPersistentData().putInt(EXPOSURE_TAG, 0);
    }

    public static int getExposureLevel(Player player) {
        return player.getPersistentData().getInt(EXPOSURE_LEVEL_TAG);
    }

    public static void setExposureLevel(Player player, int level) {
        if(level < 0) {
            level = 0; // Ne pas permettre de niveau négatif
        } else if(level > 8) {
            level = 8; // Limiter le niveau maximum
        }
        player.getPersistentData().putInt(EXPOSURE_LEVEL_TAG, level);
    }

    public static void incrementExposureLevel(Player player) {
        int currentLevel = getExposureLevel(player);
        setExposureLevel(player, currentLevel + 1);
    }
}