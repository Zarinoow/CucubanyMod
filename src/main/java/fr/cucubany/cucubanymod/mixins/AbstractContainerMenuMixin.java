package fr.cucubany.cucubanymod.mixins;

import fr.cucubany.cucubanymod.bank.CoinValue;
import fr.cucubany.cucubanymod.wallet.CoinSlot;
import fr.cucubany.cucubanymod.wallet.PileSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.NonNullList;

/**
 * Mixin sur AbstractContainerMenu pour gérer les comportements spéciaux
 * des slots portefeuille (conversion/déconversion de pièces).
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Shadow
    public NonNullList<Slot> slots;

    @Shadow
    public abstract ItemStack getCarried();

    @Shadow
    public abstract void setCarried(ItemStack pStack);

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void onWalletClickHead(int pSlotId, int pButton, ClickType pClickType, Player pPlayer,
                                   CallbackInfo ci) {
        if (pSlotId < 0 || pSlotId >= slots.size()) return;

        Slot slot = slots.get(pSlotId);
        if (!slot.isActive()) return;
        if (!(slot instanceof CoinSlot) && !(slot instanceof PileSlot)) return;

        // ── Clic droit : laisser vanilla gérer (dépôt 1 par 1, pick up moitié) ──
        // Pas de cancel, vanilla s'en occupe. onWalletClickReturn fera l'auto-conversion.

        // ── Shift-click : prendre incrémentalement depuis le slot ──
        if (pClickType == ClickType.QUICK_MOVE) {
            if (slot instanceof CoinSlot coinSlot) {
                handleShiftClickCoinSlot(coinSlot);
            } else if (slot instanceof PileSlot pileSlot) {
                handleShiftClickPileSlot(pileSlot);
            }
            autoConvertCoins();
            ci.cancel();
            return;
        }

        // ── Clic gauche : tout gérer manuellement pour éviter le swap vanilla ──
        if (pClickType == ClickType.PICKUP && pButton == 0) {
            if (slot instanceof CoinSlot coinSlot) {
                handleLeftClickCoinSlot(coinSlot);
            } else if (slot instanceof PileSlot pileSlot) {
                handleLeftClickPileSlot(pileSlot);
            }
            autoConvertCoins();
            ci.cancel();
        }
    }

    // ── Left-click CoinSlot ──
    private void handleLeftClickCoinSlot(CoinSlot coinSlot) {
        ItemStack carried = getCarried();
        ItemStack slotStack = coinSlot.getItem();

        if (carried.isEmpty() && slotStack.isEmpty()) {
            // Deux vides → décraftrer 1 pile en 9 pièces
            PileSlot pileSlot = coinSlot.getPairedPileSlot();
            if (pileSlot != null && pileSlot.isActive() && !pileSlot.getItem().isEmpty()) {
                ItemStack pileStack = pileSlot.getItem().copy();
                CoinValue pileValue = CoinValue.fromItem(pileStack.getItem());
                if (pileValue != null && pileValue.isPile()) {
                    CoinValue singleValue = pileValue.getPairedDenomination();
                    Item singleItem = singleValue != null ? singleValue.getItem() : null;
                    if (singleItem != null) {
                        pileStack.shrink(1);
                        pileSlot.set(pileStack.isEmpty() ? ItemStack.EMPTY : pileStack);
                        setCarried(new ItemStack(singleItem, 9));
                    }
                }
            }
        } else if (carried.isEmpty()) {
            // Curseur vide → ramasser tout le slot
            setCarried(slotStack.copy());
            coinSlot.set(ItemStack.EMPTY);
        } else if (slotStack.isEmpty()) {
            // Slot vide → déposer si accepté
            if (coinSlot.mayPlace(carried)) {
                coinSlot.set(carried.copy());
                setCarried(ItemStack.EMPTY);
            }
        } else if (carried.getItem() == slotStack.getItem()) {
            // Même item → merger (remplir le slot, garder le surplus sur le curseur)
            int max = coinSlot.getMaxStackSize();
            int total = slotStack.getCount() + carried.getCount();
            int inSlot = Math.min(total, max);
            int onCursor = total - inSlot;
            coinSlot.set(new ItemStack(slotStack.getItem(), inSlot));
            setCarried(onCursor == 0 ? ItemStack.EMPTY : new ItemStack(carried.getItem(), onCursor));
        }
        // Items différents → ne rien faire (pas de swap)
    }

    // ── Left-click PileSlot ──
    private void handleLeftClickPileSlot(PileSlot pileSlot) {
        ItemStack carried = getCarried();
        ItemStack slotStack = pileSlot.getItem();

        if (carried.isEmpty() && !slotStack.isEmpty()) {
            // Curseur vide → ramasser tout le slot
            setCarried(slotStack.copy());
            pileSlot.set(ItemStack.EMPTY);
        } else if (!carried.isEmpty() && slotStack.isEmpty()) {
            // Slot vide → déposer si accepté
            if (pileSlot.mayPlace(carried)) {
                pileSlot.set(carried.copy());
                setCarried(ItemStack.EMPTY);
            }
        } else if (!carried.isEmpty() && !slotStack.isEmpty() && carried.getItem() == slotStack.getItem()) {
            // Même item → merger avec gestion overflow
            int max = pileSlot.getMaxStackSize();
            int total = slotStack.getCount() + carried.getCount();
            int inSlot = Math.min(total, max);
            int surplus = total - inSlot;
            pileSlot.set(new ItemStack(slotStack.getItem(), inSlot));

            // Tenter de convertir le surplus en pièces
            int remainingOnCursor = surplus;
            if (surplus > 0) {
                CoinValue carriedCv = CoinValue.fromItem(carried.getItem());
                CoinSlot coinSlot = pileSlot.getPairedCoinSlot();
                if (carriedCv != null && coinSlot != null && coinSlot.isActive()) {
                    CoinValue singleValue = carriedCv.getPairedDenomination();
                    Item singleItem = singleValue != null ? singleValue.getItem() : null;
                    if (singleItem != null) {
                        ItemStack coinStack = coinSlot.getItem();
                        if (coinStack.isEmpty() || coinStack.getItem() == singleItem) {
                            int currentCoins = coinStack.isEmpty() ? 0 : coinStack.getCount();
                            int spaceInCoinSlot = coinSlot.getMaxStackSize() - currentCoins;
                            int pilesToConvert = Math.min(surplus, spaceInCoinSlot / 9);
                            if (pilesToConvert > 0) {
                                coinSlot.set(new ItemStack(singleItem, currentCoins + pilesToConvert * 9));
                                remainingOnCursor -= pilesToConvert;
                            }
                        }
                    }
                }
            }

            setCarried(remainingOnCursor == 0 ? ItemStack.EMPTY : new ItemStack(carried.getItem(), remainingOnCursor));
        }
        // Items différents → ne rien faire (pas de swap)
    }

    // ── Shift-click CoinSlot : prendre 9 pièces (décraftrer 1 pile si nécessaire) ──
    private void handleShiftClickCoinSlot(CoinSlot coinSlot) {
        ItemStack carried = getCarried();
        ItemStack coinStack = coinSlot.getItem();

        // Déterminer le type de pièce (depuis le CoinSlot ou le PileSlot)
        Item coinItem = null;
        CoinValue coinCv = null;
        if (!coinStack.isEmpty()) {
            coinCv = CoinValue.fromItem(coinStack.getItem());
            if (coinCv != null && !coinCv.isPile()) coinItem = coinCv.getItem();
        }
        if (coinItem == null) {
            PileSlot pileSlot = coinSlot.getPairedPileSlot();
            if (pileSlot != null && !pileSlot.getItem().isEmpty()) {
                CoinValue pileValue = CoinValue.fromItem(pileSlot.getItem().getItem());
                if (pileValue != null && pileValue.isPile()) {
                    coinCv = pileValue.getPairedDenomination();
                    if (coinCv != null) coinItem = coinCv.getItem();
                }
            }
        }
        if (coinItem == null) return;
        if (!carried.isEmpty() && carried.getItem() != coinItem) return;

        int currentInCursor = carried.isEmpty() ? 0 : carried.getCount();
        int spaceInCursor = 64 - currentInCursor;
        if (spaceInCursor <= 0) return;

        int toGive = Math.min(9, spaceInCursor);
        int given = 0;

        // Étape 1 : prendre depuis le CoinSlot
        if (!coinStack.isEmpty()) {
            int fromSlot = Math.min(coinStack.getCount(), toGive);
            int remaining = coinStack.getCount() - fromSlot;
            coinSlot.set(remaining == 0 ? ItemStack.EMPTY : new ItemStack(coinItem, remaining));
            given += fromSlot;
        }

        // Étape 2 : si pas assez, décraftrer 1 pile
        if (given < toGive) {
            PileSlot pileSlot = coinSlot.getPairedPileSlot();
            if (pileSlot != null && !pileSlot.getItem().isEmpty()) {
                CoinValue pileValue = CoinValue.fromItem(pileSlot.getItem().getItem());
                if (pileValue != null && pileValue.isPile() && pileValue.getPairedDenomination() == coinCv) {
                    ItemStack pileStack = pileSlot.getItem().copy();
                    pileStack.shrink(1);
                    pileSlot.set(pileStack.isEmpty() ? ItemStack.EMPTY : pileStack);
                    int needed = toGive - given;
                    int fromPile = Math.min(9, needed);
                    given += fromPile;
                    // Remettre les pièces excédentaires dans le CoinSlot
                    int leftover = 9 - fromPile;
                    if (leftover > 0) {
                        ItemStack existing = coinSlot.getItem();
                        int existingCount = existing.isEmpty() ? 0 : existing.getCount();
                        coinSlot.set(new ItemStack(coinItem, existingCount + leftover));
                    }
                }
            }
        }

        if (given > 0) {
            setCarried(new ItemStack(coinItem, currentInCursor + given));
        }
    }

    // ── Shift-click PileSlot : prendre 1 pile ──
    private void handleShiftClickPileSlot(PileSlot pileSlot) {
        ItemStack carried = getCarried();
        ItemStack pileStack = pileSlot.getItem();
        if (pileStack.isEmpty()) return;
        if (!carried.isEmpty() && carried.getItem() != pileStack.getItem()) return;

        int currentInCursor = carried.isEmpty() ? 0 : carried.getCount();
        if (currentInCursor >= 64) return;

        int remaining = pileStack.getCount() - 1;
        pileSlot.set(remaining == 0 ? ItemStack.EMPTY : new ItemStack(pileStack.getItem(), remaining));
        setCarried(new ItemStack(pileStack.getItem(), currentInCursor + 1));
    }

    // ── Auto-conversion : ≥9 pièces dans un CoinSlot → piles dans le PileSlot apparié ──
    private void autoConvertCoins() {
        for (Slot s : slots) {
            if (!(s instanceof CoinSlot coinSlot) || !coinSlot.isActive()) continue;

            ItemStack coinStack = coinSlot.getItem();
            if (coinStack.isEmpty() || coinStack.getCount() < 9) continue;

            PileSlot pileSlot = coinSlot.getPairedPileSlot();
            if (pileSlot == null || !pileSlot.isActive()) continue;

            CoinValue coinValue = CoinValue.fromItem(coinStack.getItem());
            if (coinValue == null || coinValue.isPile()) continue;

            CoinValue pileValue = coinValue.getPairedDenomination();
            if (pileValue == null) continue;

            Item pileItem = pileValue.getItem();
            if (pileItem == null) continue;

            int count = coinStack.getCount();
            int pilesToCreate = count / 9;
            int remainingCoins = count % 9;

            ItemStack currentPileStack = pileSlot.getItem();
            int currentPileCount = 0;
            if (!currentPileStack.isEmpty()) {
                if (currentPileStack.getItem() == pileItem) {
                    currentPileCount = currentPileStack.getCount();
                } else {
                    continue;
                }
            }

            int canAccept = 64 - currentPileCount;
            int pilesConverted = Math.min(pilesToCreate, canAccept);
            if (pilesConverted == 0) continue;

            int newCoinCount = remainingCoins + (pilesToCreate - pilesConverted) * 9;
            int newPileCount = currentPileCount + pilesConverted;

            if (newCoinCount == 0) {
                coinSlot.set(ItemStack.EMPTY);
            } else {
                ItemStack updated = coinStack.copy();
                updated.setCount(newCoinCount);
                coinSlot.set(updated);
            }
            pileSlot.set(new ItemStack(pileItem, newPileCount));
        }
    }

    /**
     * Après chaque clic vanilla (right-click, etc.), auto-convertir les pièces en piles.
     */
    @Inject(method = "clicked", at = @At("RETURN"))
    private void onWalletClickReturn(int pSlotId, int pButton, ClickType pClickType, Player pPlayer,
                                     CallbackInfo ci) {
        if (pPlayer.level.isClientSide) return;
        autoConvertCoins();
    }
}
