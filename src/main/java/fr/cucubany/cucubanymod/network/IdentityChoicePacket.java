package fr.cucubany.cucubanymod.network;

import net.minecraft.network.FriendlyByteBuf;

public class IdentityChoicePacket {
    private final String firstName;
    private final String lastName;

    public IdentityChoicePacket(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static void encode(IdentityChoicePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.firstName);
        buffer.writeUtf(packet.lastName);
    }

    public static IdentityChoicePacket decode(FriendlyByteBuf buffer) {
        String firstName = buffer.readUtf(32767);
        String lastName = buffer.readUtf(32767);
        return new IdentityChoicePacket(firstName, lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
