package fr.cucubany.cucubanymod.wallet;

import fr.cucubany.cucubanymod.bank.CoinValue;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Slot droit d'une paire portefeuille : accepte uniquement des piles de pièces.
 * Se verrouille sur la dénomination en cours (dérivée du CoinSlot apparié).
 */
public class PileSlot extends WalletSlot {

    private CoinSlot pairedCoinSlot;

    public PileSlot(Container container, int index, int x, int y, Player player) {
        super(container, index, x, y, player);
    }

    public void setPairedCoinSlot(CoinSlot coinSlot) {
        this.pairedCoinSlot = coinSlot;
    }

    public CoinSlot getPairedCoinSlot() {
        return pairedCoinSlot;
    }

    /**
     * Retourne la valeur de pile actuellement verrouillée pour cette paire,
     * ou null si aucun des deux slots ne contient quoi que ce soit.
     */
    @Nullable
    public CoinValue getLockedPileValue() {
        // Vérifier ce slot d'abord
        ItemStack myStack = getItem();
        if (!myStack.isEmpty()) {
            CoinValue cv = CoinValue.fromItem(myStack.getItem());
            if (cv != null && cv.isPile()) return cv;
        }
        // Puis dériver depuis le CoinSlot apparié
        if (pairedCoinSlot != null) {
            CoinValue lockedCoin = pairedCoinSlot.getLockedCoinValue();
            if (lockedCoin != null) return lockedCoin.getPairedDenomination();
        }
        return null;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!isActive()) return false;
        CoinValue cv = CoinValue.fromItem(stack.getItem());
        if (cv == null || !cv.isPile()) return false; // refuser les pièces simples et non-pièces
        CoinValue locked = getLockedPileValue();
        return locked == null || locked == cv;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}
