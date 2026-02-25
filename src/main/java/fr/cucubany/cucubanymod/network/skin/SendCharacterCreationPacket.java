package fr.cucubany.cucubanymod.network.skin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.networking.PacketSendGenderInfo;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.IdentityUpdatePacket;
import fr.cucubany.cucubanymod.config.CucubanyCommonConfigs;
import fr.cucubany.cucubanymod.roleplay.GenderOption;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import fr.cucubany.cucubanymod.roleplay.skin.CharacterAppearance;
import fr.cucubany.cucubanymod.server.ServerSkinBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Supplier;

public class SendCharacterCreationPacket {
    private final CharacterAppearance appearance;

    public SendCharacterCreationPacket(CharacterAppearance appearance) {
        this.appearance = appearance;
    }

    public static void encode(SendCharacterCreationPacket msg, FriendlyByteBuf buf) {
        CharacterAppearance.encode(msg.appearance, buf);
    }

    public static SendCharacterCreationPacket decode(FriendlyByteBuf buf) {
        return new SendCharacterCreationPacket(CharacterAppearance.decode(buf));
    }

    public static void handle(SendCharacterCreationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            UUID uuid = player.getUUID();
            CucubanyMod.getLogger().info("Received skin for player: {}", player.getName().getString());

            // 1. Génération de la texture Serveur
            BufferedImage skinImage = ServerSkinBuilder.buildSkin(msg.appearance);

            // 2. Définition du dossier de sauvegarde
            // Chemin : ./saves/MonMonde/cucubanymod/wardrobe/{uuid}/
            ServerLevel level = player.getLevel();
            File worldDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File wardrobeDir = new File(worldDir, "cucubanymod/wardrobe/" + uuid.toString());

            if (!wardrobeDir.exists()) {
                wardrobeDir.mkdirs();
            }

            // 3. Sauvegarde de l'image (skin_0.png par exemple, ou current.png)
            File skinFile = new File(wardrobeDir, "current_skin.png");
            try {
                ImageIO.write(skinImage, "png", skinFile);
                CucubanyMod.getLogger().info("Saved skin for player {}: {}", player.getName().getString(), skinFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Identity identity = IdentityProvider.getIdentity(player);
            if (identity != null) {
                identity.setSlim(msg.appearance.isSlim());
            }

            try {
                GenderPlayer wPlayer = WildfireGender.getOrAddPlayerById(player.getUUID());

                // Paramètres choisis par le joueur
                wPlayer.updateGender(msg.appearance.getGender());
                wPlayer.updateBustSize(msg.appearance.getBustSize());
                wPlayer.getBreasts().updateXOffset(msg.appearance.getBustXOffset());
                wPlayer.getBreasts().updateYOffset(msg.appearance.getBustYOffset());
                wPlayer.getBreasts().updateZOffset(msg.appearance.getBustZOffset());
                wPlayer.getBreasts().updateCleavage(msg.appearance.getBustCleavage());

                // Valeurs fixes depuis la config serveur (communes à tous, cachées au joueur)
                wPlayer.updateBounceMultiplier((float) CucubanyCommonConfigs.BREAST_BOUNCE_MULTIPLIER.get().doubleValue());
                wPlayer.updateFloppiness((float) CucubanyCommonConfigs.BREAST_FLOPPY_MULTIPLIER.get().doubleValue());
                wPlayer.updateBreastPhysics(CucubanyCommonConfigs.BREAST_PHYSICS.get());
                wPlayer.updateArmorBreastPhysics(CucubanyCommonConfigs.BREAST_PHYSICS_ARMOR.get());
                wPlayer.updateShowBreastsInArmor(CucubanyCommonConfigs.BREAST_SHOW_IN_ARMOR.get());
                wPlayer.updateHurtSounds(CucubanyCommonConfigs.BREAST_HURT_SOUNDS.get());
                wPlayer.getBreasts().updateUniboob(CucubanyCommonConfigs.BREAST_UNIBOOB.get());

                GenderPlayer.saveGenderInfo(wPlayer);
                PacketSendGenderInfo.send(wPlayer);

                // Sauvegarde complète dans la capability du joueur
                if (identity != null) {
                    GenderOption go = identity.getGenderOption();
                    go.setGender(msg.appearance.getGender());
                    go.setBustSize(msg.appearance.getBustSize());
                    go.setXOffset(msg.appearance.getBustXOffset());
                    go.setYOffset(msg.appearance.getBustYOffset());
                    go.setZOffset(msg.appearance.getBustZOffset());
                    go.setCleavage(msg.appearance.getBustCleavage());
                    go.setBounceMultiplier((float) CucubanyCommonConfigs.BREAST_BOUNCE_MULTIPLIER.get().doubleValue());
                    go.setFloppyMultiplier((float) CucubanyCommonConfigs.BREAST_FLOPPY_MULTIPLIER.get().doubleValue());
                    go.setBreastPhysics(CucubanyCommonConfigs.BREAST_PHYSICS.get());
                    go.setBreastPhysicsArmor(CucubanyCommonConfigs.BREAST_PHYSICS_ARMOR.get());
                    go.setShowInArmor(CucubanyCommonConfigs.BREAST_SHOW_IN_ARMOR.get());
                    go.setHurtSounds(CucubanyCommonConfigs.BREAST_HURT_SOUNDS.get());
                    go.setUniboob(CucubanyCommonConfigs.BREAST_UNIBOOB.get());
                }

            } catch (Exception e) {
                CucubanyMod.getLogger().error("Error while applying Wildfire gender", e);
            }

            // 4. Sauvegarde de la configuration JSON (pour ré-ouvrir l'éditeur plus tard)
            File jsonFile = new File(wardrobeDir, "current_config.json");
            try (FileWriter writer = new FileWriter(jsonFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(msg.appearance, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                byte[] data = Files.readAllBytes(skinFile.toPath());

                // On notifie tous les joueurs qui voient ce joueur (TRACKING_ENTITY)
                SyncSkinPacket updatePacket = new SyncSkinPacket(player.getUUID(), data, msg.appearance.isSlim());
                CucubanyPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), updatePacket);
                IdentityUpdatePacket identityPacket = new IdentityUpdatePacket(player.getUUID(), identity);
                CucubanyPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), identityPacket);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}