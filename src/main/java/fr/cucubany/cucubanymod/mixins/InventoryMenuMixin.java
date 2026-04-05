package fr.cucubany.cucubanymod.mixins;

import fr.cucubany.cucubanymod.wallet.IWalletCapability;
import fr.cucubany.cucubanymod.wallet.WalletCapabilityProvider;
import fr.cucubany.cucubanymod.wallet.WalletSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import net.minecraft.world.inventory.InventoryMenu;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu {

    protected InventoryMenuMixin(@Nullable MenuType<?> type, int id) {
        super(type, id);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addWalletSlots(Inventory playerInventory, boolean isLocalPlayer, Player player, CallbackInfo ci) {
        player.getCapability(WalletCapabilityProvider.WALLET_CAPABILITY).ifPresent(cap -> {
            var container = cap.getContainer();
            int[] ys = {47, 64, 81, 98, 115};

            // Slot 0 : carte d'identité
            addSlot(new WalletSlot(container, 0, -154, 105));
            // Slots 1-5 : pièces
            for (int i = 0; i < 5; i++) {
                addSlot(new WalletSlot(container, 1 + i, -106, ys[i]));
            }
            // Slots 6-10 : piles de pièces
            for (int i = 0; i < 5; i++) {
                addSlot(new WalletSlot(container, 6 + i, -88, ys[i]));
            }
        });
    }
}
