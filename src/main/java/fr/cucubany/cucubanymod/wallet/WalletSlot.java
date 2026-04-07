package fr.cucubany.cucubanymod.wallet;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class WalletSlot extends Slot {
    private final Player player;

    public WalletSlot(Container container, int index, int x, int y, Player player) {
        super(container, index, x, y);
        this.player = player;
    }

    @Override
    public boolean isActive() {
        return WalletState.walletOpen && !player.isCreative();
    }
}
