package fr.cucubany.cucubanymod.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record OpenSkillScreenPacket(UUID playerUUID) {

    public static void encode(OpenSkillScreenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerUUID);
    }

    public static OpenSkillScreenPacket decode(FriendlyByteBuf buffer) {
        return new OpenSkillScreenPacket(buffer.readUUID());
    }

}
