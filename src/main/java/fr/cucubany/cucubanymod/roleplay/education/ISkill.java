package fr.cucubany.cucubanymod.roleplay.education;

import net.minecraft.network.chat.TranslatableComponent;

public interface ISkill {

    String getKeyName();

    TranslatableComponent getDisplayName();
    TranslatableComponent getDisplayDescription();

    int getLevel();

    String getDisplayIcon();

    void setLevel(int level);

    default void resetLevel() {
        setLevel(0);
    }

    default void increaseLevel() {
        setLevel(getLevel() + 1);
    }

    default void decreaseLevel() {
        setLevel(getLevel() - 1);
    }

}
