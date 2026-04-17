package fr.cucubany.cucubanymod.wallet;

import fr.cucubany.cucubanymod.items.CucubanyItems;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Slot réservé à la carte d'identité uniquement.
 */
public class IdentitySlot extends WalletSlot {

    public IdentitySlot(Container container, int index, int x, int y, Player player) {
        super(container, index, x, y, player);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!isActive()) return false;
        return stack.getItem() == CucubanyItems.IDENTITY_CARD.get();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
