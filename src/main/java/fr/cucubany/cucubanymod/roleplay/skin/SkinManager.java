package fr.cucubany.cucubanymod.roleplay.skin;

import com.mojang.blaze3d.platform.NativeImage;
import fr.cucubany.cucubanymod.roleplay.skin.custom.CharacterOption;
import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SkinManager {

    public static ResourceLocation generateSkin(List<CharacterOption> selectedParts, Map<SkinPart, Integer> tintColors) {
        NativeImage skinImage = new NativeImage(64, 64, true);

        // Tri par renderPriority : valeur faible = calque du bas, valeur élevée = par-dessus
        selectedParts.sort(Comparator.comparingInt(o -> o.category().getRenderPriority()));

        try {
            for (CharacterOption part : selectedParts) {
                if (part.textureData() == null) continue;

                NativeImage partImage = NativeImage.read(new ByteArrayInputStream(part.textureData()));

                // Récupération de la couleur (Blanc par défaut si non définie)
                int tintColor = tintColors.getOrDefault(part.category(), 0xFFFFFFFF);

                // DÉTECTION DU FORMAT 16x16 (Yeux, Sourcils, Bouche...)
                if (partImage.getWidth() == 16 && partImage.getHeight() == 16) {
                    processQuadTexture(skinImage, partImage, tintColor);
                } else {
                    // Format standard 64x64 (Corps, Cheveux, Vêtements)
                    processStandardTexture(skinImage, partImage, tintColor);
                }

                partImage.close();
            }

            String skinId = "generated_skin_" + System.currentTimeMillis();
            ResourceLocation location = new ResourceLocation("cucubanymod", skinId);
            Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(skinImage));
            return location;

        } catch (IOException e) {
            e.printStackTrace();
            return new ResourceLocation("minecraft", "textures/entity/steve.png");
        }
    }

    /**
     * Applique une texture standard 64x64 sur le skin.
     */
    private static void processStandardTexture(NativeImage target, NativeImage source, int tintColor) {
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                int color = source.getPixelRGBA(x, y);
                int alpha = (color >> 24) & 0xFF;

                if (alpha > 0) {
                    if (tintColor != 0xFFFFFFFF) {
                        color = multiplyColor(color, tintColor);
                    }
                    target.setPixelRGBA(x, y, color);
                }
            }
        }
    }

    /**
     * Traite le format compact 16x16 (4 quadrants).
     * Mappe les pixels du 16x16 vers les zones Visage (8,8) et Overlay (40,8) du skin 64x64.
     */
    private static void processQuadTexture(NativeImage target, NativeImage source, int tintColor) {
        // Offsets cibles sur la texture 64x64 de Minecraft
        int faceStartX = 8;
        int faceStartY = 8;
        int overlayStartX = 40; // Couche 'Chapeau' (Hat layer)
        int overlayStartY = 8;

        // On parcourt la grille 8x8 (taille d'un visage)
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {

                // --- ZONE 1 : VISAGE + TEINTE (Haut-Gauche) ---
                // Exemple : Iris des yeux
                int p1 = source.getPixelRGBA(x, y);
                if (((p1 >> 24) & 0xFF) > 0) {
                    int c = (tintColor != 0xFFFFFFFF) ? multiplyColor(p1, tintColor) : p1;
                    target.setPixelRGBA(faceStartX + x, faceStartY + y, c);
                }

                // --- ZONE 2 : VISAGE + STATIQUE (Haut-Droite) ---
                // Exemple : Blanc des yeux
                int p2 = source.getPixelRGBA(x + 8, y);
                if (((p2 >> 24) & 0xFF) > 0) {
                    target.setPixelRGBA(faceStartX + x, faceStartY + y, p2); // Pas de teinte
                }

                // --- ZONE 3 : OVERLAY + TEINTE (Bas-Gauche) ---
                // Exemple : Sourcils (relief + couleur cheveux)
                int p3 = source.getPixelRGBA(x, y + 8);
                if (((p3 >> 24) & 0xFF) > 0) {
                    int c = (tintColor != 0xFFFFFFFF) ? multiplyColor(p3, tintColor) : p3;
                    target.setPixelRGBA(overlayStartX + x, overlayStartY + y, c);
                }

                // --- ZONE 4 : OVERLAY + STATIQUE (Bas-Droite) ---
                // Exemple : Accessoires fixes
                int p4 = source.getPixelRGBA(x + 8, y + 8);
                if (((p4 >> 24) & 0xFF) > 0) {
                    target.setPixelRGBA(overlayStartX + x, overlayStartY + y, p4);
                }
            }
        }
    }

    private static int multiplyColor(int textureColorABGR, int tintColorARGB) {
        int a1 = (textureColorABGR >> 24) & 0xFF;
        int b1 = (textureColorABGR >> 16) & 0xFF;
        int g1 = (textureColorABGR >> 8) & 0xFF;
        int r1 = (textureColorABGR) & 0xFF;

        int r2 = (tintColorARGB >> 16) & 0xFF;
        int g2 = (tintColorARGB >> 8) & 0xFF;
        int b2 = (tintColorARGB) & 0xFF;

        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;

        return (a1 << 24) | (b << 16) | (g << 8) | r;
    }
}