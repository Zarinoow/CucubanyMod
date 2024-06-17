package fr.cucubany.cucubanymod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class CucubanyPacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("cucubanymod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, IdentityChoicePacket.class, IdentityChoicePacket::encode, IdentityChoicePacket::decode, IdentityChoicePacketHandler::handle);
        INSTANCE.registerMessage(id++, OpenIdentityScreenPacket.class, (packet, buffer) -> {}, buffer -> new OpenIdentityScreenPacket(), OpenIdentityScreenPacketHandler::handle);
        INSTANCE.registerMessage(id++, IdentityUpdatePacket.class, IdentityUpdatePacket::encode, IdentityUpdatePacket::decode, IdentityUpdatePacketHandler::handle);
    }
}
