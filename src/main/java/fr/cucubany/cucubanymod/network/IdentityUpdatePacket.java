package fr.cucubany.cucubanymod.network;

import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.education.IAspiration;
import fr.cucubany.cucubanymod.roleplay.education.Skill;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record IdentityUpdatePacket(UUID uuid, Identity identity) {

    public static void encode(IdentityUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.uuid);
        buffer.writeUtf(packet.identity.getFirstName());
        buffer.writeUtf(packet.identity.getLastName());
        for(var skill : packet.identity.getEducation().getSkills()) {
            buffer.writeUtf(skill.getKeyName());
            buffer.writeInt(skill.getLevel());
            if(skill instanceof IAspiration) {
                // TODO: Encode aspiration
            }
        }
    }

    public static IdentityUpdatePacket decode(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        String firstName = buffer.readUtf(32767);
        String lastName = buffer.readUtf(32767);
        Identity identity = new Identity(firstName, lastName);
        while (buffer.readableBytes() > 0) {
            String skillName = buffer.readUtf(32767);
            Skill skill = identity.getEducation().getSkills().stream().filter(s -> s.getKeyName().equals(skillName)).findFirst().orElse(null);
            if(skill != null) {
                skill.setLevel(buffer.readInt());
                if (skill instanceof IAspiration) {
                    // TODO: Decode aspiration
                }
            }
        }
        return new IdentityUpdatePacket(uuid, identity);
    }

}
