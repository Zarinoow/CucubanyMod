package fr.cucubany.cucubanymod.wallet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;

public interface IWalletCapability {
    int SIZE = 11;
    SimpleContainer getContainer();
    CompoundTag writeNBT();
    void readNBT(CompoundTag tag);
}
