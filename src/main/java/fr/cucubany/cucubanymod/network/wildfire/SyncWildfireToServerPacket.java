package fr.cucubany.cucubanymod.network.wildfire;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.networking.PacketSendGenderInfo;
import fr.cucubany.cucubanymod.CucubanyMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paquet envoyé par le CLIENT vers le SERVEUR lors de la connexion.
 * Transporte les paramètres Wildfire (poitrine) du joueur afin de
 * resynchroniser le serveur avec les données potentiellement modifiées en solo.
 */
public class SyncWildfireToServerPacket {

    private final GenderPlayer.Gender gender;
    private final float bustSize;
    private final float bounceMultiplier;
    private final float floppyMultiplier;
    private final boolean breastPhysics;
    private final boolean breastPhysicsArmor;
    private final boolean showInArmor;
    private final boolean hurtSounds;
    private final float xOffset;
    private final float yOffset;
    private final float zOffset;
    private final float cleavage;
    private final boolean uniboob;

    public SyncWildfireToServerPacket(GenderPlayer genderPlayer) {
        this.gender = genderPlayer.getGender();
        this.bustSize = genderPlayer.getBustSize();
        this.bounceMultiplier = genderPlayer.getBounceMultiplierRaw();
        this.floppyMultiplier = genderPlayer.getFloppiness();
        this.breastPhysics = genderPlayer.hasBreastPhysics();
        this.breastPhysicsArmor = genderPlayer.hasArmorBreastPhysics();
        this.showInArmor = genderPlayer.showBreastsInArmor();
        this.hurtSounds = genderPlayer.hasHurtSounds();
        this.xOffset = genderPlayer.getBreasts().getXOffset();
        this.yOffset = genderPlayer.getBreasts().getYOffset();
        this.zOffset = genderPlayer.getBreasts().getZOffset();
        this.cleavage = genderPlayer.getBreasts().getCleavage();
        this.uniboob = genderPlayer.getBreasts().isUniboob();
    }

    private SyncWildfireToServerPacket(GenderPlayer.Gender gender, float bustSize,
                                        float bounceMultiplier, float floppyMultiplier,
                                        boolean breastPhysics, boolean breastPhysicsArmor,
                                        boolean showInArmor, boolean hurtSounds,
                                        float xOffset, float yOffset, float zOffset,
                                        float cleavage, boolean uniboob) {
        this.gender = gender;
        this.bustSize = bustSize;
        this.bounceMultiplier = bounceMultiplier;
        this.floppyMultiplier = floppyMultiplier;
        this.breastPhysics = breastPhysics;
        this.breastPhysicsArmor = breastPhysicsArmor;
        this.showInArmor = showInArmor;
        this.hurtSounds = hurtSounds;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.cleavage = cleavage;
        this.uniboob = uniboob;
    }

    public static void encode(SyncWildfireToServerPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.gender);
        buf.writeFloat(packet.bustSize);
        buf.writeFloat(packet.bounceMultiplier);
        buf.writeFloat(packet.floppyMultiplier);
        buf.writeBoolean(packet.breastPhysics);
        buf.writeBoolean(packet.breastPhysicsArmor);
        buf.writeBoolean(packet.showInArmor);
        buf.writeBoolean(packet.hurtSounds);
        buf.writeFloat(packet.xOffset);
        buf.writeFloat(packet.yOffset);
        buf.writeFloat(packet.zOffset);
        buf.writeFloat(packet.cleavage);
        buf.writeBoolean(packet.uniboob);
    }

    public static SyncWildfireToServerPacket decode(FriendlyByteBuf buf) {
        return new SyncWildfireToServerPacket(
                buf.readEnum(GenderPlayer.Gender.class),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readBoolean()
        );
    }

    public static void handle(SyncWildfireToServerPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            try {
                GenderPlayer wPlayer = WildfireGender.getOrAddPlayerById(player.getUUID());

                wPlayer.updateGender(packet.gender);
                wPlayer.updateBustSize(packet.bustSize);
                wPlayer.updateBounceMultiplier(packet.bounceMultiplier);
                wPlayer.updateFloppiness(packet.floppyMultiplier);
                wPlayer.updateBreastPhysics(packet.breastPhysics);
                wPlayer.updateArmorBreastPhysics(packet.breastPhysicsArmor);
                wPlayer.updateShowBreastsInArmor(packet.showInArmor);
                wPlayer.updateHurtSounds(packet.hurtSounds);
                wPlayer.getBreasts().updateXOffset(packet.xOffset);
                wPlayer.getBreasts().updateYOffset(packet.yOffset);
                wPlayer.getBreasts().updateZOffset(packet.zOffset);
                wPlayer.getBreasts().updateCleavage(packet.cleavage);
                wPlayer.getBreasts().updateUniboob(packet.uniboob);

                // Sauvegarde sur disque côté serveur
                GenderPlayer.saveGenderInfo(wPlayer);

                // Broadcast aux joueurs qui voient ce joueur
                PacketSendGenderInfo.send(wPlayer);

                // Stockage dans le persistentData du joueur (survit aux morts/dimensions)
                CompoundTag wildfireTag = new CompoundTag();
                wildfireTag.putString("gender", packet.gender.name());
                wildfireTag.putFloat("bustSize", packet.bustSize);
                wildfireTag.putFloat("bounceMultiplier", packet.bounceMultiplier);
                wildfireTag.putFloat("floppyMultiplier", packet.floppyMultiplier);
                wildfireTag.putBoolean("breastPhysics", packet.breastPhysics);
                wildfireTag.putBoolean("breastPhysicsArmor", packet.breastPhysicsArmor);
                wildfireTag.putBoolean("showInArmor", packet.showInArmor);
                wildfireTag.putBoolean("hurtSounds", packet.hurtSounds);
                wildfireTag.putFloat("xOffset", packet.xOffset);
                wildfireTag.putFloat("yOffset", packet.yOffset);
                wildfireTag.putFloat("zOffset", packet.zOffset);
                wildfireTag.putFloat("cleavage", packet.cleavage);
                wildfireTag.putBoolean("uniboob", packet.uniboob);
                player.getPersistentData().put("cucubany_wildfire", wildfireTag);

                CucubanyMod.getLogger().debug("Wildfire data synced from client for player: {}", player.getName().getString());

            } catch (Exception e) {
                CucubanyMod.getLogger().error("Error syncing Wildfire data from client for player: {}", player.getName().getString(), e);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}