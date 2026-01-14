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

        buffer.writeBoolean(packet.identity.isSlim());

        var skills = packet.identity.getEducation().getSkills();
        buffer.writeInt(skills.size());

        for(var skill : skills) {
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

        boolean isSlim = buffer.readBoolean();

        Identity identity = new Identity(firstName, lastName);
        identity.setSlim(isSlim);

        // --- SÉCURITÉ : On lit le nombre exact de skills ---
        int skillCount = buffer.readInt();

        for (int i = 0; i < skillCount; i++) {
            String skillName = buffer.readUtf(32767);
            int level = buffer.readInt();

            // On cherche le skill dans l'identité par défaut pour le mettre à jour
            Skill skill = identity.getEducation().getSkills().stream()
                    .filter(s -> s.getKeyName().equals(skillName))
                    .findFirst()
                    .orElse(null);

            if(skill != null) {
                skill.setLevel(level);
                if (skill instanceof IAspiration) {
                    // TODO: Decode aspiration
                }
            }
        }
        return new IdentityUpdatePacket(uuid, identity);
    }
}