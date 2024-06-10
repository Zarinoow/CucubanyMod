package fr.cucubany.cucubanymod.capabilities;

import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.education.Skill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public interface IIdentityCapability {
    void setIdentity(Identity identity);
    Identity getIdentity();

    default Tag writeTag() {
        CompoundTag tag = new CompoundTag();
        Identity identity = getIdentity();
        if (identity != null) {
            tag.putString("firstName", identity.getFirstName());
            tag.putString("lastName", identity.getLastName());
            for(Skill skill : identity.getEducation().getSkills()) {
                tag.putInt(skill.getKeyName(), skill.getLevel());
            }
        }
        return tag;
    }

    default void readTag(Tag nbt) {
        CompoundTag tag = (CompoundTag) nbt;
        String firstName = tag.getString("firstName");
        String lastName = tag.getString("lastName");
        Identity id = new Identity(firstName, lastName);
        for(Skill skill : id.getEducation().getSkills()) {
            skill.setLevel(tag.getInt(skill.getKeyName()));
        }
        setIdentity(id);
    }
}
