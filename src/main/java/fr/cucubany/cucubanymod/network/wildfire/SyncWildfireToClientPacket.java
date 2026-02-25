package fr.cucubany.cucubanymod.network.wildfire;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.roleplay.GenderOption;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paquet envoyé par le SERVEUR vers le CLIENT lors de la connexion.
 * Impose au client les données de poitrine sauvegardées dans la capability du joueur,
 * écrasant la configuration locale si elle a été modifiée.
 */
public class SyncWildfireToClientPacket {

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

    public SyncWildfireToClientPacket(GenderOption genderOption) {
        this.gender = genderOption.getGender();
        this.bustSize = genderOption.getBustSize();
        this.bounceMultiplier = genderOption.getBounceMultiplier();
        this.floppyMultiplier = genderOption.getFloppyMultiplier();
        this.breastPhysics = genderOption.hasBreastPhysics();
        this.breastPhysicsArmor = genderOption.hasBreastPhysicsArmor();
        this.showInArmor = genderOption.isShowInArmor();
        this.hurtSounds = genderOption.hasHurtSounds();
        this.xOffset = genderOption.getXOffset();
        this.yOffset = genderOption.getYOffset();
        this.zOffset = genderOption.getZOffset();
        this.cleavage = genderOption.getCleavage();
        this.uniboob = genderOption.isUniboob();
    }

    private SyncWildfireToClientPacket(GenderPlayer.Gender gender, float bustSize,
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

    public static void encode(SyncWildfireToClientPacket packet, FriendlyByteBuf buf) {
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

    public static SyncWildfireToClientPacket decode(FriendlyByteBuf buf) {
        return new SyncWildfireToClientPacket(
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

    public static void handle(SyncWildfireToClientPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet))
        );
        ctx.get().setPacketHandled(true);
    }

    private static void handleClient(SyncWildfireToClientPacket packet) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            GenderPlayer wPlayer = WildfireGender.getOrAddPlayerById(mc.player.getUUID());

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

            // Sauvegarde côté client (écrase la config locale)
            GenderPlayer.saveGenderInfo(wPlayer);

            CucubanyMod.getLogger().debug("Wildfire data overwritten from server for local player.");
        } catch (Exception e) {
            CucubanyMod.getLogger().error("Error applying server Wildfire data to local player.", e);
        }
    }
}