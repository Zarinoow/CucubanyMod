package fr.cucubany.cucubanymod.wallet;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class WalletSlot extends Slot {
    public WalletSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean isActive() {
        return WalletState.walletOpen;
    }
}
