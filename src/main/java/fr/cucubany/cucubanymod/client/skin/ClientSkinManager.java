package fr.cucubany.cucubanymod.client.skin;

import com.mojang.blaze3d.platform.NativeImage;
import fr.cucubany.cucubanymod.CucubanyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientSkinManager {

    private static final Map<UUID, ResourceLocation> PLAYER_SKINS = new HashMap<>();
    private static final Map<UUID, Boolean> PLAYER_MODELS = new HashMap<>();

    public static void registerSkin(UUID uuid, byte[] data, boolean isSlim) {
        if (data == null || data.length == 0) return;

        // On force l'exécution sur le Thread de rendu (RenderThread)
        Minecraft.getInstance().execute(() -> {
            try {
                NativeImage image = NativeImage.read(new ByteArrayInputStream(data));

                // On crée la texture
                DynamicTexture texture = new DynamicTexture(image);

                // ID unique basé sur l'UUID
                ResourceLocation location = new ResourceLocation(CucubanyMod.MOD_ID, "player_skin/" + uuid.toString().toLowerCase());

                // Enregistrement officiel
                Minecraft.getInstance().getTextureManager().register(location, texture);

                // Stockage
                PLAYER_SKINS.put(uuid, location);
                PLAYER_MODELS.put(uuid, isSlim);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static ResourceLocation getLocationSkin(UUID uuid) {
        return PLAYER_SKINS.get(uuid);
    }

    public static boolean isPlayerSlim(UUID uuid) {
        return PLAYER_MODELS.getOrDefault(uuid, false);
    }

    public static boolean hasCustomSkin(UUID uuid) {
        return PLAYER_SKINS.containsKey(uuid);
    }
}