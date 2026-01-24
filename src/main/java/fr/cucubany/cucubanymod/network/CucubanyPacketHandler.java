package fr.cucubany.cucubanymod.network;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.network.hitbox.SyncBodyHealthPacket;
import fr.cucubany.cucubanymod.network.hitbox.SyncPartIdsPacket;
import fr.cucubany.cucubanymod.network.skin.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class CucubanyPacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CucubanyMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        // Identity packets
        INSTANCE.registerMessage(id++, IdentityChoicePacket.class, IdentityChoicePacket::encode, IdentityChoicePacket::decode, IdentityChoicePacketHandler::handle);
        INSTANCE.registerMessage(id++, OpenIdentityScreenPacket.class, (packet, buffer) -> {}, buffer -> new OpenIdentityScreenPacket(), OpenIdentityScreenPacketHandler::handle);
        INSTANCE.registerMessage(id++, IdentityUpdatePacket.class, IdentityUpdatePacket::encode, IdentityUpdatePacket::decode, IdentityUpdatePacketHandler::handle);
        // Skill packets
        INSTANCE.registerMessage(id++, OpenSkillScreenPacket.class, OpenSkillScreenPacket::encode, OpenSkillScreenPacket::decode, OpenSkillScreenPacketHandler::handle);
        // Skin packets
        INSTANCE.registerMessage(id++, RequestSkinContentPacket.class, RequestSkinContentPacket::encode, RequestSkinContentPacket::decode, RequestSkinContentPacket::handle);
        INSTANCE.registerMessage(id++, SendCharacterCreationPacket.class, SendCharacterCreationPacket::encode, SendCharacterCreationPacket::decode, SendCharacterCreationPacket::handle);
        INSTANCE.registerMessage(id++, SyncSkinPacket.class, SyncSkinPacket::encode, SyncSkinPacket::decode, SyncSkinPacket::handle);
        INSTANCE.registerMessage(id++, CustomizationOptionsPacket.class, CustomizationOptionsPacket::encode, CustomizationOptionsPacket::decode, CustomizationOptionsPacketHandler::handle);
        // Body health packets
        INSTANCE.registerMessage(id++, SyncBodyHealthPacket.class, SyncBodyHealthPacket::encode, SyncBodyHealthPacket::decode, SyncBodyHealthPacket::handle);
        INSTANCE.registerMessage(id++, SyncPartIdsPacket.class, SyncPartIdsPacket::encode, SyncPartIdsPacket::decode, SyncPartIdsPacket::handle);
    }
}
