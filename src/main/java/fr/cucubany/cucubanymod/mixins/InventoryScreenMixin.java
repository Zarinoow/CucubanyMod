package fr.cucubany.cucubanymod.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.wallet.WalletState;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    @Shadow @Final private RecipeBookComponent recipeBookComponent;
    @Shadow private boolean widthTooNarrow;

    @Unique private static final ResourceLocation WALLET_TEXTURE =
            new ResourceLocation(CucubanyMod.MOD_ID, "textures/gui/wallet.png");
    @Unique private static final int WALLET_W = 178;
    @Unique private static final int WALLET_H = 168;
    /** Premier index de slot wallet dans le menu (46 slots vanilla avant). */
    @Unique private static final int WALLET_FIRST_SLOT = 46;

    @Unique private ImageButton walletButton;
    @Unique private ImageButton recipeBookButton;
    @Unique private boolean walletHandled = false;
    @Unique private boolean cucubanymod_wasRecipeBookVisible = false;

    protected InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Inject(method = "init", at = @At("RETURN"))
    private void initWallet(CallbackInfo ci) {
        recipeBookButton = null;
        for (GuiEventListener listener : children()) {
            if (listener instanceof ImageButton btn) {
                recipeBookButton = btn;
                break;
            }
        }

        walletButton = new ImageButton(
                this.leftPos + 126, this.height / 2 - 22, 20, 18,
                72, 220, 18, WALLET_TEXTURE,
                (btn) -> {
                    if (recipeBookComponent.isVisible()) {
                        recipeBookComponent.toggleVisibility();
                    }
                    WalletState.walletOpen = !WalletState.walletOpen;
                    walletHandled = true;
                    this.leftPos = cucubanymod_updateScreenPosition();
                    cucubanymod_repositionButtons();
                }
        );
        this.addRenderableWidget(walletButton);

        this.leftPos = cucubanymod_updateScreenPosition();
        cucubanymod_repositionButtons();
    }

    // ── Toggle livre de recettes ──────────────────────────────────────────────

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void beforeMouseClicked(double mouseX, double mouseY, int btn,
                                     CallbackInfoReturnable<Boolean> cir) {
        cucubanymod_wasRecipeBookVisible = recipeBookComponent.isVisible();
        walletHandled = false;
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void afterMouseClicked(double mouseX, double mouseY, int btn,
                                    CallbackInfoReturnable<Boolean> cir) {
        if (!walletHandled) {
            boolean toggled = recipeBookComponent.isVisible() != cucubanymod_wasRecipeBookVisible;
            if (toggled) {
                if (recipeBookComponent.isVisible() && WalletState.walletOpen) {
                    WalletState.walletOpen = false;
                }
                this.leftPos = cucubanymod_updateScreenPosition();
                cucubanymod_repositionButtons();
            }
        }
        walletHandled = false;
    }

    // ── Rendu : fond du wallet + fonds des slots (AVANT le rendu des items) ──

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"))
    private void renderWalletBackground(PoseStack poseStack, int mouseX, int mouseY,
                                         float partialTick, CallbackInfo ci) {
        if (!WalletState.walletOpen || widthTooNarrow) return;

        int wx = this.leftPos - WALLET_W - 4;
        int wy = this.topPos + (this.imageHeight - WALLET_H) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, WALLET_TEXTURE);

        // Panneau principal
        blit(poseStack, wx, wy, 0, 0, WALLET_W, WALLET_H);

        // Fonds des slots (different UV selon type de slot et présence d'item)
        if (this.menu.slots.size() > WALLET_FIRST_SLOT + 10) {
            int[] localYs = {47, 64, 81, 98, 115};

            // Slot 0 : carte d'identité  (UV empty=0,220  filled=54,220)
            boolean id0 = !this.menu.slots.get(WALLET_FIRST_SLOT).getItem().isEmpty();
            blit(poseStack, wx + 27, wy + 105, id0 ? 54 : 0, 220, 18, 18);

            // Slots 1-5 : pièces  (UV empty=18,220)
            for (int i = 0; i < 5; i++) {
                boolean has = !this.menu.slots.get(WALLET_FIRST_SLOT + 1 + i).getItem().isEmpty();
                blit(poseStack, wx + 75, wy + localYs[i], has ? 54 : 18, 220, 18, 18);
            }

            // Slots 6-10 : piles  (UV empty=36,220)
            for (int i = 0; i < 5; i++) {
                boolean has = !this.menu.slots.get(WALLET_FIRST_SLOT + 6 + i).getItem().isEmpty();
                blit(poseStack, wx + 93, wy + localYs[i], has ? 54 : 36, 220, 18, 18);
            }
        }
    }

    // ── Clic dans le wallet = pas "hors GUI" ──────────────────────────────────

    @Inject(method = "hasClickedOutside", at = @At("RETURN"), cancellable = true)
    private void walletHasClickedOutside(double mouseX, double mouseY,
                                          int guiLeft, int guiTop, int mouseBtn,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (WalletState.walletOpen && !widthTooNarrow && Boolean.TRUE.equals(cir.getReturnValue())) {
            int wx = this.leftPos - WALLET_W - 4;
            int wy = this.topPos + (this.imageHeight - WALLET_H) / 2;
            if (mouseX >= wx && mouseY >= wy && mouseX < wx + WALLET_W && mouseY < wy + WALLET_H) {
                cir.setReturnValue(false);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @Unique
    private int cucubanymod_updateScreenPosition() {
        if (WalletState.walletOpen && !widthTooNarrow) {
            return (this.width - this.imageWidth - 182) / 2 + 182;
        } else if (recipeBookComponent.isVisible() && !widthTooNarrow) {
            return recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        } else {
            return (this.width - this.imageWidth) / 2;
        }
    }

    @Unique
    private void cucubanymod_repositionButtons() {
        if (recipeBookButton != null) {
            recipeBookButton.setPosition(this.leftPos + 104, this.height / 2 - 22);
        }
        if (walletButton != null) {
            walletButton.setPosition(this.leftPos + 126, this.height / 2 - 22);
        }
    }
}
