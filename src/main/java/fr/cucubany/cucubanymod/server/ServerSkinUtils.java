package fr.cucubany.cucubanymod.server;

import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ServerSkinUtils {

    public static byte[] getPlayerSkinBytes(ServerPlayer player) {
        try {
            ServerLevel level = player.getLevel();
            File worldDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            // Le même chemin que lors de la sauvegarde
            File skinFile = new File(worldDir, "cucubanymod/wardrobe/" + player.getUUID().toString() + "/current_skin.png");

            if (skinFile.exists()) {
                return FileUtils.readFileToByteArray(skinFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Pas de skin custom
    }

    public static boolean isPlayerSlim(ServerPlayer player) {
        Identity identity = IdentityProvider.getIdentity(player);
        if (identity != null) {
            return identity.isSlim();
        }
        return false;
    }
}