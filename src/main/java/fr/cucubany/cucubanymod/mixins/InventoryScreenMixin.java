package fr.cucubany.cucubanymod.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.cucubany.cucubanymod.CucubanyMod;
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

    // ── Champs shadowed de InventoryScreen ────────────────────────────────────
    @Shadow @Final private RecipeBookComponent recipeBookComponent;
    @Shadow private boolean widthTooNarrow;

    // ── Texture et dimensions du portefeuille ─────────────────────────────────
    @Unique private static final ResourceLocation WALLET_TEXTURE =
            new ResourceLocation(CucubanyMod.MOD_ID, "textures/gui/wallet.png");
    @Unique private static final int WALLET_W = 178;
    @Unique private static final int WALLET_H = 168;

    /**
     * État persistant entre les ouvertures/fermetures de l'inventaire,
     * comme le livre de recettes qui mémorise s'il était ouvert.
     */
    @Unique private static boolean walletVisible = false;

    @Unique private ImageButton walletButton;
    @Unique private ImageButton recipeBookButton;

    /**
     * Vrai quand le bouton portefeuille vient de gérer le clic lui-même,
     * pour éviter que l'injection mouseClicked RETURN ne traite le même événement.
     */
    @Unique private boolean walletHandled = false;
    @Unique private boolean cucubanymod_wasRecipeBookVisible = false;

    // ── Constructeur fictif requis par le compilateur ─────────────────────────
    protected InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Inject(method = "init", at = @At("RETURN"))
    private void initWallet(CallbackInfo ci) {
        // Retrouver le bouton du livre de recettes (premier ImageButton ajouté par vanilla)
        recipeBookButton = null;
        for (GuiEventListener listener : children()) {
            if (listener instanceof ImageButton btn) {
                recipeBookButton = btn;
                break;
            }
        }

        // Bouton portefeuille à droite du bouton livre de recettes (même hauteur, même taille)
        // Livre de recettes : leftPos+104, h/2-22, 20×18 → portefeuille : leftPos+126, h/2-22, 20×18
        walletButton = new ImageButton(
                this.leftPos + 126, this.height / 2 - 22, 20, 18,
                74, 222, 18, WALLET_TEXTURE,
                (btn) -> {
                    if (recipeBookComponent.isVisible()) {
                        recipeBookComponent.toggleVisibility();
                    }
                    walletVisible = !walletVisible;
                    walletHandled = true;
                    this.leftPos = cucubanymod_updateScreenPosition();
                    cucubanymod_repositionButtons();
                }
        );
        this.addRenderableWidget(walletButton);

        // Appliquer le bon leftPos (redimensionnement alors que le portefeuille était ouvert)
        this.leftPos = cucubanymod_updateScreenPosition();
        cucubanymod_repositionButtons();
    }

    // ── Synchronisation lors du toggle du livre de recettes ───────────────────

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void beforeMouseClicked(double mouseX, double mouseY, int btn,
                                     CallbackInfoReturnable<Boolean> cir) {
        cucubanymod_wasRecipeBookVisible = recipeBookComponent.isVisible();
        walletHandled = false;
    }

    /**
     * Appelé après chaque mouseClicked.
     * Si le livre de recettes vient d'être togglé par son propre bouton (pas par le
     * bouton portefeuille), on ferme le portefeuille si nécessaire et on repositionne
     * tous les boutons en une seule passe — sans décalage visuel sur deux frames.
     */
    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void afterMouseClicked(double mouseX, double mouseY, int btn,
                                    CallbackInfoReturnable<Boolean> cir) {
        if (!walletHandled) {
            boolean recipeBookToggled =
                    recipeBookComponent.isVisible() != cucubanymod_wasRecipeBookVisible;
            if (recipeBookToggled) {
                // Le livre de recettes s'est ouvert : fermer le portefeuille
                if (recipeBookComponent.isVisible() && walletVisible) {
                    walletVisible = false;
                }
                this.leftPos = cucubanymod_updateScreenPosition();
                cucubanymod_repositionButtons();
            }
        }
        walletHandled = false;
    }

    // ── Rendu du panneau portefeuille ─────────────────────────────────────────

    @Inject(method = "render", at = @At("RETURN"))
    private void renderWallet(PoseStack poseStack, int mouseX, int mouseY,
                               float partialTick, CallbackInfo ci) {
        if (!walletVisible || widthTooNarrow) return;

        int wx = this.leftPos - WALLET_W - 4;
        int wy = this.topPos + (this.imageHeight - WALLET_H) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, WALLET_TEXTURE);
        blit(poseStack, wx, wy, 0, 0, WALLET_W, WALLET_H);
    }

    // ── Clic dans le portefeuille = pas "hors GUI" (évite de jeter des items) ─

    @Inject(method = "hasClickedOutside", at = @At("RETURN"), cancellable = true)
    private void walletHasClickedOutside(double mouseX, double mouseY,
                                          int guiLeft, int guiTop, int mouseBtn,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (walletVisible && !widthTooNarrow && Boolean.TRUE.equals(cir.getReturnValue())) {
            int wx = this.leftPos - WALLET_W - 4;
            int wy = this.topPos + (this.imageHeight - WALLET_H) / 2;
            if (mouseX >= wx && mouseY >= wy && mouseX < wx + WALLET_W && mouseY < wy + WALLET_H) {
                cir.setReturnValue(false);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Retourne le leftPos approprié selon quel panneau est ouvert.
     * Portefeuille ouvert → décale l'inventaire pour centrer wallet(178) + gap(4) + inv(176) = 358px.
     * Livre de recettes ouvert → délègue à la logique vanilla.
     * Rien d'ouvert → centrage simple.
     */
    @Unique
    private int cucubanymod_updateScreenPosition() {
        if (walletVisible && !widthTooNarrow) {
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
