package fr.cucubany.cucubanymod.server;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.roleplay.skin.CharacterAppearance;
import fr.cucubany.cucubanymod.roleplay.skin.custom.CharacterOption;
import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ServerSkinBuilder {

    public static BufferedImage buildSkin(CharacterAppearance appearance) {
        // 1. Création de la texture vide 64x64
        BufferedImage finalSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = finalSkin.createGraphics();

        // 2. Récupération des options depuis le cache serveur via les ID
        List<CharacterOption> optionsToApply = new ArrayList<>();

        for (Map.Entry<SkinPart, String> entry : appearance.getSelectedParts().entrySet()) {
            // On demande au ServerSkinManager de trouver l'objet complet via son ID
            CharacterOption option = ServerSkinManager.getOptionById(entry.getKey(), entry.getValue());
            if (option != null) {
                optionsToApply.add(option);
            }
        }

        boolean hasPants = appearance.getSelectedParts().containsKey(SkinPart.PANTS);
        if (!hasPants) {
            // On demande au Manager de nous donner le fichier 'underwear_default' chargé dans la catégorie PANTS
            CharacterOption underwear = ServerSkinManager.getOptionById(SkinPart.PANTS, "default");

            if (underwear != null) {
                optionsToApply.add(underwear);
                CucubanyMod.getLogger().warn("The player skin is missing pants! Applying default underwear.");
            } else {
                CucubanyMod.getLogger().warn("WARNING: Player skin is missing pants and default underwear texture is not found in the folder 'config/cucubanymod/skins/pants/' on the server!");
            }
        }

        // 3. Tri par renderPriority : valeur faible = calque du bas, valeur élevée = par-dessus
        optionsToApply.sort(Comparator.comparingInt(o -> o.category().getRenderPriority()));

        // 4. Application des calques
        for (CharacterOption option : optionsToApply) {
            try {
                BufferedImage layer = ImageIO.read(new ByteArrayInputStream(option.textureData()));
                int tintColor = appearance.getPartColors().getOrDefault(option.category(), 0xFFFFFFFF);

                if (layer.getWidth() == 16 && layer.getHeight() == 16) {
                    processQuadTexture(finalSkin, layer, tintColor);
                } else {
                    processStandardTexture(finalSkin, layer, tintColor);
                }
            } catch (IOException e) {
                CucubanyMod.getLogger().warn("Error reading server texture for " + option.id());
            }
        }

        g2d.dispose();
        return finalSkin;
    }

    // Logique standard 64x64
    private static void processStandardTexture(BufferedImage target, BufferedImage source, int tintColor) {
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                int argb = source.getRGB(x, y);
                if ((argb >> 24 & 0xFF) > 0) { // Si pas transparent
                    if (tintColor != 0xFFFFFFFF) {
                        argb = multiplyColor(argb, tintColor);
                    }
                    target.setRGB(x, y, argb);
                }
            }
        }
    }

    // Logique propriétaire 16x16 (4 quadrants)
    private static void processQuadTexture(BufferedImage target, BufferedImage source, int tintColor) {
        int faceX = 8, faceY = 8;
        int overlayX = 40, overlayY = 8;

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                // 1. Face Color (Haut-G)
                applyPixel(target, source, x, y, faceX + x, faceY + y, tintColor, true);
                // 2. Face Static (Haut-D)
                applyPixel(target, source, x + 8, y, faceX + x, faceY + y, 0xFFFFFFFF, false);
                // 3. Overlay Color (Bas-G)
                applyPixel(target, source, x, y + 8, overlayX + x, overlayY + y, tintColor, true);
                // 4. Overlay Static (Bas-D)
                applyPixel(target, source, x + 8, y + 8, overlayX + x, overlayY + y, 0xFFFFFFFF, false);
            }
        }
    }

    private static void applyPixel(BufferedImage target, BufferedImage source, int srcX, int srcY, int destX, int destY, int tint, boolean useTint) {
        int argb = source.getRGB(srcX, srcY);
        if ((argb >> 24 & 0xFF) > 0) {
            if (useTint && tint != 0xFFFFFFFF) {
                argb = multiplyColor(argb, tint);
            }
            target.setRGB(destX, destY, argb);
        }
    }

    // Mathématiques des couleurs version AWT
    private static int multiplyColor(int c1, int c2) {
        int a1 = (c1 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = (c1) & 0xFF;

        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = (c2) & 0xFF;

        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;

        return (a1 << 24) | (r << 16) | (g << 8) | b;
    }
}