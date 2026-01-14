package fr.cucubany.cucubanymod.client.skin;

import com.mojang.blaze3d.platform.NativeImage;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.roleplay.skin.custom.CharacterOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cette classe gère la partie VISUELLE des CharacterOption.
 * Elle ne doit être appelée QUE depuis des classes Clients (GUI, Renderer).
 */
public class ClientSkinHelper {

    // Cache pour éviter de recréer la texture 60 fois par seconde
    private static final Map<CharacterOption, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    public static ResourceLocation getTextureLocation(CharacterOption option) {
        if (option == null) return null;

        // 1. Vérifier si on a déjà chargé cette texture
        if (TEXTURE_CACHE.containsKey(option)) {
            return TEXTURE_CACHE.get(option);
        }

        // 2. Création de la texture
        try {
            byte[] data = option.textureData();
            if (data == null || data.length == 0) return null;

            NativeImage image = NativeImage.read(new ByteArrayInputStream(data));

            // On génère un nom unique pour TextureManager
            ResourceLocation location = new ResourceLocation(CucubanyMod.MOD_ID,
                    "textures/character/" + option.category().name().toLowerCase() + "/" + option.id() + "_" + System.identityHashCode(option));

            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            textureManager.register(location, new DynamicTexture(image));

            // On stocke dans le cache
            TEXTURE_CACHE.put(option, location);

            return location;

        } catch (IOException e) {
            e.printStackTrace();
            // Texture de secours (Steve) en cas d'erreur
            return new ResourceLocation("minecraft", "textures/entity/steve.png");
        }
    }

    // Utile pour nettoyer la mémoire si nécessaire (ex: changement de serveur)
    public static void clearCache() {
        TEXTURE_CACHE.clear();
    }
}
