package fr.cucubany.cucubanymod.wallet;

import fr.cucubany.cucubanymod.bank.CoinValue;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Slot gauche d'une paire portefeuille : accepte uniquement des pièces simples
 * (pas de piles). Se verrouille sur la dénomination en cours dès qu'une pièce
 * ou une pile est présente dans la paire.
 */
public class CoinSlot extends WalletSlot {

    private PileSlot pairedPileSlot;

    public CoinSlot(Container container, int index, int x, int y, Player player) {
        super(container, index, x, y, player);
    }

    public void setPairedPileSlot(PileSlot pileSlot) {
        this.pairedPileSlot = pileSlot;
    }

    public PileSlot getPairedPileSlot() {
        return pairedPileSlot;
    }

    /**
     * Retourne la valeur de pièce (simple, non-pile) actuellement verrouillée
     * pour cette paire, ou null si aucun des deux slots ne contient quoi que ce soit.
     */
    @Nullable
    public CoinValue getLockedCoinValue() {
        // Vérifier ce slot d'abord
        ItemStack myStack = getItem();
        if (!myStack.isEmpty()) {
            CoinValue cv = CoinValue.fromItem(myStack.getItem());
            if (cv != null && !cv.isPile()) return cv;
        }
        // Puis le slot piles apparié
        if (pairedPileSlot != null) {
            ItemStack pileStack = pairedPileSlot.getItem();
            if (!pileStack.isEmpty()) {
                CoinValue cv = CoinValue.fromItem(pileStack.getItem());
                if (cv != null && cv.isPile()) return cv.getPairedDenomination();
            }
        }
        return null;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!isActive()) return false;
        CoinValue cv = CoinValue.fromItem(stack.getItem());
        if (cv == null || cv.isPile()) return false; // refuser les piles et les non-pièces
        CoinValue locked = getLockedCoinValue();
        return locked == null || locked == cv;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}
