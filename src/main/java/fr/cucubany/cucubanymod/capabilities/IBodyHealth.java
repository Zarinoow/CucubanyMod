package fr.cucubany.cucubanymod.capabilities;

import fr.cucubany.cucubanymod.hitbox.BodyPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IBodyHealth extends INBTSerializable<CompoundTag> {

    // Obtenir la vie d'une partie
    float getHealth(BodyPart part);

    // Définir la vie d'une partie
    void setHealth(BodyPart part, float amount);

    // Blesser une partie
    void hurt(BodyPart part, float amount);

    // Soigner une partie
    void heal(BodyPart part, float amount);

    // Obtenir la vie max (par défaut 20.0 pour tout le monde ou variable ?)
    float getMaxHealth(BodyPart part);

    // Réinitialiser tout (Respawn)
    void reset();
}
