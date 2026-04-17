package fr.cucubany.cucubanymod.mixins;

import fr.cucubany.cucubanymod.wallet.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu {

    protected InventoryMenuMixin(@Nullable MenuType<?> type, int id) {
        super(type, id);
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void handleWalletSlotQuickMove(Player pPlayer, int pIndex, CallbackInfoReturnable<ItemStack> cir) {
        if (pIndex < 0 || pIndex >= slots.size()) return;
        Slot slot = slots.get(pIndex);
        if (!(slot instanceof WalletSlot)) return;

        // Bloquer le quickMoveStack vanilla pour les wallet slots
        // La logique est gérée dans AbstractContainerMenuMixin.onWalletClickHead
        cir.setReturnValue(ItemStack.EMPTY);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addWalletSlots(Inventory playerInventory, boolean isLocalPlayer, Player player, CallbackInfo ci) {
        player.getCapability(WalletCapabilityProvider.WALLET_CAPABILITY).ifPresent(cap -> {
            var container = cap.getContainer();
            int[] ys = {47, 64, 81, 98, 115};

            // Slot 0 : carte d'identité
            addSlot(new IdentitySlot(container, 0, -154, 105, player));

            // Créer les 5 paires et les apparier
            CoinSlot[] coinSlots = new CoinSlot[5];
            PileSlot[] pileSlots = new PileSlot[5];
            for (int i = 0; i < 5; i++) {
                coinSlots[i] = new CoinSlot(container, 1 + i, -106, ys[i], player);
                pileSlots[i] = new PileSlot(container, 6 + i, -88, ys[i], player);
                coinSlots[i].setPairedPileSlot(pileSlots[i]);
                pileSlots[i].setPairedCoinSlot(coinSlots[i]);
            }

            // Slots 1-5 : pièces, puis 6-10 : piles (ordre important pour les indices conteneur)
            for (CoinSlot cs : coinSlots) addSlot(cs);
            for (PileSlot ps : pileSlots) addSlot(ps);
        });
    }
}
