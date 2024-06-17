package fr.cucubany.cucubanymod.network;

import net.minecraft.network.FriendlyByteBuf;

public record IdentityChoicePacket(String firstName, String lastName) {

    public static void encode(IdentityChoicePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.firstName);
        buffer.writeUtf(packet.lastName);
    }

    public static IdentityChoicePacket decode(FriendlyByteBuf buffer) {
        String firstName = buffer.readUtf(32767);
        String lastName = buffer.readUtf(32767);
        return new IdentityChoicePacket(firstName, lastName);
    }
}
