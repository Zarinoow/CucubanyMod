package fr.cucubany.cucubanymod.client.screen;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import com.wildfire.main.GenderPlayer;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.animation.ClientAnimationManager;
import fr.cucubany.cucubanymod.client.skin.ClientSkinHelper;
import fr.cucubany.cucubanymod.client.skin.WildfireBridge;
import fr.cucubany.cucubanymod.client.widgets.GradientColorPicker;
import fr.cucubany.cucubanymod.config.CucubanyCommonConfigs;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.skin.SendCharacterCreationPacket;
import fr.cucubany.cucubanymod.roleplay.GenderOption;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import fr.cucubany.cucubanymod.roleplay.dummy.DummyPlayer;
import fr.cucubany.cucubanymod.roleplay.skin.CharacterAppearance;
import fr.cucubany.cucubanymod.roleplay.skin.SkinManager;
import fr.cucubany.cucubanymod.roleplay.skin.custom.CharacterOption;
import fr.cucubany.cucubanymod.roleplay.skin.custom.CustomizationData;
import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.*;

public class CharacterCustomizationScreen extends Screen {

    private static final ResourceLocation BUTTON_BG_TEXTURE = new ResourceLocation(CucubanyMod.MOD_ID, "textures/gui/character_option_background.png");

    // --- PALETTES DE COULEURS ---
    private static final int[] PALETTE_SKIN = { 0xFFF5E6, 0xFFDFC4, 0xE0B084, 0x8D5524, 0x593623, 0x2F1B0E };
    private static final int[] PALETTE_HAIR = { 0xFFFFFF, 0x999999, 0x111111, 0xEBC673, 0xA52A2A, 0x4B3621, 0x2244AA, 0x228822, 0x882288 };
    private static final int[] PALETTE_EYES = { 0x66CCFF, 0x228822, 0x663300, 0xCC3333, 0x9933CC, 0xFFFF00 };

    // --- LAYOUT CONSTANTS ---
    private static final int RIGHT_PANEL_WIDTH = 150;
    private static final int LEFT_PANEL_WIDTH  = 110;
    private static final int ITEM_SIZE         = 40;
    private static final int ITEM_SPACING      = 5;
    private static final int ITEMS_PER_ROW     = 3;

    // --- POPUP MORPHOLOGIE ---
    private static final int   POPUP_W            = 180;
    private static final int   POPUP_H            = 210;
    private static final float POPUP_ROTATION_Y   = 25f;
    private boolean morphologyPanelOpen = false;

    // Layout variables dynamiques (calculées dans init)
    private int popupX, popupY, actualPopupW, actualPopupH;

    // --- Variables d'état ---
    private float modelRotationY = 0f;
    private float modelRotationX = 0f;
    private float modelZoom      = 60.0f;
    private SkinPart currentCategory = SkinPart.BODY;

    private final Map<SkinPart, CharacterOption> currentSelections = new HashMap<>();
    private final Map<SkinPart, Integer>         categoryColors    = new HashMap<>();
    private final Map<SkinPart, Double>          colorPickerValues = new HashMap<>();

    private boolean isUpdatingWidget        = false;
    private boolean isSlimModel             = false;
    private boolean hasInitialSkinGenerated = false;

    private CharacterOption cachedUnderwear = null;

    // Sexe du personnage
    private GenderPlayer.Gender currentGender = GenderPlayer.Gender.MALE;

    // Morphologie poitrine
    private float bustSize     = 0.5f;
    private float bustXOffset  = 0.0f;
    private float bustYOffset  = 0.0f;
    private float bustZOffset  = 0.0f;
    private float bustCleavage = 0.05f;

    // Scroll pour la matrice de droite
    private float currentScroll = 0f;

    private final DummyPlayer dummyPlayer;
    private final DummyPlayer previewDummy;
    private final ClientLevel clientLevel;
    private final boolean     isFirstCreation;

    // Widgets principaux
    private Button             sexButton;
    private Button             morphologyButton;
    private GradientColorPicker colorPicker;
    private int currentTintForPreview = 0xFFFFFFFF;

    // Widgets du popup morphologie
    private Button              modelTypePopupButton;
    private AbstractSliderButton sliderBustSize;
    private AbstractSliderButton sliderXOffset;
    private AbstractSliderButton sliderYOffset;
    private AbstractSliderButton sliderZOffset;
    private AbstractSliderButton sliderCleavage;

    // Dragging
    private boolean isDraggingModel = false;

    public CharacterCustomizationScreen(LocalPlayer player, boolean isFirstCreation) {
        super(new TranslatableComponent("screen.cucubanymod.character_editor.title"));
        this.clientLevel    = player.clientLevel;
        this.isFirstCreation = isFirstCreation;
        this.dummyPlayer    = new DummyPlayer(clientLevel, new GameProfile(UUID.randomUUID(), "DummyPlayer"));
        this.previewDummy   = new DummyPlayer(clientLevel, new GameProfile(UUID.randomUUID(), "PreviewDummy"));

        if (!isFirstCreation) {
            Identity identity = IdentityProvider.getIdentity(player);
            if (identity != null) {
                GenderOption go = identity.getGenderOption();
                this.currentGender  = go.getGender();
                this.isSlimModel    = identity.isSlim();
                this.bustSize       = go.getBustSize();
                this.bustXOffset    = go.getXOffset();
                this.bustYOffset    = go.getYOffset();
                this.bustZOffset    = go.getZOffset();
                this.bustCleavage   = go.getCleavage();
            }
        }

        initializeDefaults();
    }

    private void initializeDefaults() {
        for (SkinPart part : SkinPart.values()) {
            colorPickerValues.put(part, 0.0);
        }
        categoryColors.put(SkinPart.BODY,     PALETTE_SKIN[0]);
        categoryColors.put(SkinPart.HAIR,     PALETTE_HAIR[0]);
        categoryColors.put(SkinPart.EYEBROWS, PALETTE_HAIR[0]);
        categoryColors.put(SkinPart.EYES,     PALETTE_EYES[0]);
    }

    @Override
    protected void init() {
        // --- 1. BOUTONS INFÉRIEURS GAUCHE ---
        int bottomButtonY = this.height - 30;
        int sexButtonY    = bottomButtonY - 25;

        this.sexButton = this.addRenderableWidget(new Button(10, sexButtonY, 76, 20,
                new TextComponent(""),
                btn -> {
                    this.currentGender = switch (this.currentGender) {
                        case MALE   -> GenderPlayer.Gender.FEMALE;
                        case FEMALE -> GenderPlayer.Gender.OTHER;
                        default     -> GenderPlayer.Gender.MALE;
                    };
                    updateSexButtonText();
                    WildfireBridge.setPlayerGender(this.dummyPlayer, this.currentGender);
                    updatePopupWidgets();
                    updateBreastPreview();
                }));
        updateSexButtonText();

        this.morphologyButton = this.addRenderableWidget(new Button(88, sexButtonY, 16, 20,
                new TranslatableComponent("screen.cucubanymod.character_editor.morphology.button"),
                btn -> {
                    morphologyPanelOpen = !morphologyPanelOpen;
                    updatePopupWidgets();
                }));

        WildfireBridge.setPlayerGender(this.dummyPlayer, this.currentGender);

        if (!isFirstCreation) {
            this.sexButton.active = false;
        }

        // --- 2. BOUTONS CATÉGORIES (RESPONSIVE) ---
        int catButtonHeight = 20; // Hauteur fixe requise par Minecraft
        int catSpacing = 5;
        int numCategories = SkinPart.values().length;
        int totalNeededHeight = numCategories * catButtonHeight + (numCategories - 1) * catSpacing;

        int buttonY = 40;
        int availableHeight = sexButtonY - 5 - buttonY; // Espace dispo

        // Si ça ne passe pas, on réduit l'espacement et on remonte l'UI
        if (totalNeededHeight > availableHeight) {
            catSpacing = 1;
            totalNeededHeight = numCategories * catButtonHeight + (numCategories - 1) * catSpacing;
            if (totalNeededHeight > availableHeight) {
                // On remonte au maximum (10 pixels du haut de l'écran)
                buttonY = Math.max(10, sexButtonY - totalNeededHeight - 5);
            }
        }

        for (SkinPart part : SkinPart.values()) {
            this.addRenderableWidget(new Button(10, buttonY, LEFT_PANEL_WIDTH - 20, catButtonHeight,
                    new TranslatableComponent("screen.cucubanymod.character_editor.category." + part.name().toLowerCase()),
                    btn -> {
                        currentCategory = part;
                        currentScroll   = 0f;
                        updateWidgetsState();
                    }));
            buttonY += catButtonHeight + catSpacing;
        }

        // --- 3. CALCULS RESPONSIVE POUR LE POPUP ---
        this.actualPopupW = POPUP_W;
        this.actualPopupH = POPUP_H;

        // Position par défaut (à gauche du panneau droit)
        this.popupX = this.width - RIGHT_PANEL_WIDTH - actualPopupW - 10;

        // SÉCURITÉ: Si le popup écrase trop l'entité (il faut min 80px pour le joueur), on décale le popup vers la droite,
        // quitte à passer pardessus le panneau de droite (qui est inactif quand le popup est ouvert).
        int minCharSpace = 80;
        if (this.popupX < LEFT_PANEL_WIDTH + minCharSpace) {
            this.popupX = LEFT_PANEL_WIDTH + minCharSpace;
        }

        this.popupY = (this.height - actualPopupH) / 2;
        if (this.popupY < 5) this.popupY = 5; // Empêche de sortir par le haut

        int sw = actualPopupW - 16;

        // --- Bouton type de corps ---
        this.modelTypePopupButton = this.addRenderableWidget(new Button(
                popupX + 8, popupY + 33, sw, 18,
                new TextComponent(""),
                btn -> {
                    isSlimModel = !isSlimModel;
                    updateModelButtonText();
                    this.dummyPlayer.setModelType(isSlimModel);
                    updateDummySkin();
                }));
        updateModelButtonText();
        if (!isFirstCreation) {
            this.modelTypePopupButton.active = false;
        }

        // --- Sliders poitrine ---
        {
            final double min  = CucubanyCommonConfigs.BREAST_SIZE_MIN.get();
            final double max  = CucubanyCommonConfigs.BREAST_SIZE_MAX.get();
            final double init = clamp01((bustSize - min) / (max - min));
            this.sliderBustSize = this.addRenderableWidget(new AbstractSliderButton(
                    popupX + 8, popupY + 74, sw, 20, new TextComponent(""), init) {
                { updateMessage(); }
                @Override protected void updateMessage() {
                    setMessage(new TextComponent(
                            I18n.get("screen.cucubanymod.character_editor.morphology.bust.size")
                                    + ": " + String.format("%d%%", (int)Math.round(value * 100))));
                }
                @Override protected void applyValue() {
                    bustSize = (float)(min + value * (max - min));
                    updateBreastPreview();
                }
            });
        }
        {
            final double min  = CucubanyCommonConfigs.BREAST_X_OFFSET_MIN.get();
            final double max  = CucubanyCommonConfigs.BREAST_X_OFFSET_MAX.get();
            final double init = clamp01((bustXOffset - min) / (max - min));
            this.sliderXOffset = this.addRenderableWidget(new AbstractSliderButton(
                    popupX + 8, popupY + 100, sw, 20, new TextComponent(""), init) {
                { updateMessage(); }
                @Override protected void updateMessage() {
                    setMessage(new TextComponent(
                            I18n.get("screen.cucubanymod.character_editor.morphology.bust.x_offset")
                                    + ": " + String.format("%d%%", (int)Math.round(value * 100))));
                }
                @Override protected void applyValue() {
                    bustXOffset = (float)(min + value * (max - min));
                    updateBreastPreview();
                }
            });
        }
        {
            final double min  = CucubanyCommonConfigs.BREAST_Y_OFFSET_MIN.get();
            final double max  = CucubanyCommonConfigs.BREAST_Y_OFFSET_MAX.get();
            final double init = clamp01((bustYOffset - min) / (max - min));
            this.sliderYOffset = this.addRenderableWidget(new AbstractSliderButton(
                    popupX + 8, popupY + 126, sw, 20, new TextComponent(""), init) {
                { updateMessage(); }
                @Override protected void updateMessage() {
                    setMessage(new TextComponent(
                            I18n.get("screen.cucubanymod.character_editor.morphology.bust.y_offset")
                                    + ": " + String.format("%d%%", (int)Math.round(value * 100))));
                }
                @Override protected void applyValue() {
                    bustYOffset = (float)(min + value * (max - min));
                    updateBreastPreview();
                }
            });
        }
        {
            final double min  = CucubanyCommonConfigs.BREAST_Z_OFFSET_MIN.get();
            final double max  = CucubanyCommonConfigs.BREAST_Z_OFFSET_MAX.get();
            final double init = clamp01((bustZOffset - min) / (max - min));
            this.sliderZOffset = this.addRenderableWidget(new AbstractSliderButton(
                    popupX + 8, popupY + 152, sw, 20, new TextComponent(""), init) {
                { updateMessage(); }
                @Override protected void updateMessage() {
                    setMessage(new TextComponent(
                            I18n.get("screen.cucubanymod.character_editor.morphology.bust.z_offset")
                                    + ": " + String.format("%d%%", (int)Math.round(value * 100))));
                }
                @Override protected void applyValue() {
                    bustZOffset = (float)(min + value * (max - min));
                    updateBreastPreview();
                }
            });
        }
        {
            final double min  = CucubanyCommonConfigs.BREAST_CLEAVAGE_MIN.get();
            final double max  = CucubanyCommonConfigs.BREAST_CLEAVAGE_MAX.get();
            final double init = clamp01((bustCleavage - min) / (max - min));
            this.sliderCleavage = this.addRenderableWidget(new AbstractSliderButton(
                    popupX + 8, popupY + 178, sw, 20, new TextComponent(""), init) {
                { updateMessage(); }
                @Override protected void updateMessage() {
                    setMessage(new TextComponent(
                            I18n.get("screen.cucubanymod.character_editor.morphology.bust.cleavage")
                                    + ": " + String.format("%d%%", (int)Math.round(value * 100))));
                }
                @Override protected void applyValue() {
                    bustCleavage = (float)(min + value * (max - min));
                    updateBreastPreview();
                }
            });
        }

        setPopupWidgetsVisible(false);

        // --- 4. Color picker ---
        int centerAreaWidth = this.width - LEFT_PANEL_WIDTH - RIGHT_PANEL_WIDTH;
        int pickerX = LEFT_PANEL_WIDTH + (centerAreaWidth - 140) / 2;
        this.colorPicker = this.addRenderableWidget(new GradientColorPicker(
                pickerX, this.height - 35, 140, 20,
                PALETTE_SKIN,
                color -> {
                    this.categoryColors.put(currentCategory, color);
                    if (!isUpdatingWidget && this.colorPicker != null) {
                        this.colorPickerValues.put(currentCategory, this.colorPicker.getValue());
                    }
                    this.currentTintForPreview = color;
                    updateDummySkin();
                }));

        // --- 5. Bouton Valider ---
        this.addRenderableWidget(new Button(this.width - RIGHT_PANEL_WIDTH + 10, this.height - 30, RIGHT_PANEL_WIDTH - 20, 20,
                new TranslatableComponent("screen.cucubanymod.character_editor.validate"),
                btn -> sendValidationPacket()));

        updateBreastPreview();
        updateWidgetsState();
        updatePopupWidgets();

        if (currentSelections.isEmpty()) {
            currentCategory = SkinPart.BODY;
            List<CharacterOption> skins = CustomizationData.getOptionsByCategory(SkinPart.BODY);
            if (!skins.isEmpty()) selectOption(skins.get(0));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasInitialSkinGenerated && !CustomizationData.getOptionsByCategory(SkinPart.BODY).isEmpty()) {
            if (!currentSelections.containsKey(SkinPart.BODY)) {
                selectOption(CustomizationData.getOptionsByCategory(SkinPart.BODY).get(0));
            }
            updateDummySkin();
            hasInitialSkinGenerated = true;
        }

        if (this.dummyPlayer != null) {
            this.dummyPlayer.tickCount++;
            ClientAnimationManager.tickLayer(this.dummyPlayer);
            WildfireBridge.tickPhysics(this.dummyPlayer);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers morphologie
    // -------------------------------------------------------------------------

    private boolean isBreastGender() {
        return currentGender == GenderPlayer.Gender.FEMALE || currentGender == GenderPlayer.Gender.OTHER;
    }

    private void updateBreastPreview() {
        WildfireBridge.applyBreastParams(this.dummyPlayer, bustSize, bustXOffset, bustYOffset, bustZOffset, bustCleavage);
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private void setPopupWidgetsVisible(boolean visible) {
        if (this.modelTypePopupButton != null) this.modelTypePopupButton.visible = visible;
        if (this.sliderBustSize  != null) this.sliderBustSize.visible  = visible;
        if (this.sliderXOffset   != null) this.sliderXOffset.visible   = visible;
        if (this.sliderYOffset   != null) this.sliderYOffset.visible   = visible;
        if (this.sliderZOffset   != null) this.sliderZOffset.visible   = visible;
        if (this.sliderCleavage  != null) this.sliderCleavage.visible  = visible;
    }

    private void updatePopupWidgets() {
        if (!morphologyPanelOpen) {
            setPopupWidgetsVisible(false);
            if (this.colorPicker != null) this.colorPicker.visible = true;
            return;
        }
        setPopupWidgetsVisible(true);
        boolean showBreast = isBreastGender();
        if (this.sliderBustSize  != null) this.sliderBustSize.visible  = showBreast;
        if (this.sliderXOffset   != null) this.sliderXOffset.visible   = showBreast;
        if (this.sliderYOffset   != null) this.sliderYOffset.visible   = showBreast;
        if (this.sliderZOffset   != null) this.sliderZOffset.visible   = showBreast;
        if (this.sliderCleavage  != null) this.sliderCleavage.visible  = showBreast;
        if (this.colorPicker     != null) this.colorPicker.visible     = false;
    }

    private void updateWidgetsState() {
        isUpdatingWidget = true;

        boolean isBody = (currentCategory == SkinPart.BODY);
        if (this.sexButton        != null) this.sexButton.visible        = isBody;
        if (this.morphologyButton != null) this.morphologyButton.visible = isBody;

        if (morphologyPanelOpen && !isBody) {
            morphologyPanelOpen = false;
            updatePopupWidgets();
        }

        if (this.colorPicker != null) {
            int[] palette = null;
            if      (currentCategory == SkinPart.BODY)     palette = PALETTE_SKIN;
            else if (currentCategory == SkinPart.HAIR)     palette = PALETTE_HAIR;
            else if (currentCategory == SkinPart.EYEBROWS) palette = PALETTE_HAIR;
            else if (currentCategory == SkinPart.EYES)     palette = PALETTE_EYES;

            if (palette != null) {
                this.colorPicker.visible = true;
                this.colorPicker.setColors(palette);
                double savedValue = colorPickerValues.getOrDefault(currentCategory, 0.0);
                this.colorPicker.setValue(savedValue);
                this.currentTintForPreview = this.colorPicker.getCurrentColor();
            } else {
                this.colorPicker.visible = false;
                this.currentTintForPreview = 0xFFFFFFFF;
            }
        }

        isUpdatingWidget = false;
    }

    private void updateModelButtonText() {
        if (this.modelTypePopupButton == null) return;
        this.modelTypePopupButton.setMessage(new TranslatableComponent(
                isSlimModel ? "screen.cucubanymod.character_editor.morphology.shape.slim"
                        : "screen.cucubanymod.character_editor.morphology.shape.normal"));
    }

    private void updateSexButtonText() {
        this.sexButton.setMessage(new TextComponent("Sexe: " + this.currentGender.getDisplayName().getString()));
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (!isDraggingModel) {
            this.modelRotationX = Mth.lerp(0.1f, this.modelRotationX, 0.0f);
            if (Math.abs(this.modelRotationX) < 0.05f) this.modelRotationX = 0.0f;
        }

        this.renderBackground(poseStack);

        fill(poseStack, 0, 0, LEFT_PANEL_WIDTH, this.height, 0x80000000);
        int rightPanelX = this.width - RIGHT_PANEL_WIDTH;
        fill(poseStack, rightPanelX, 0, this.width, this.height, 0xAA000000);

        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        // --- CALCUL RESPONSIVE DE L'ENTITÉ ---
        int centerX;
        float effectiveZoom = modelZoom;

        if (morphologyPanelOpen) {
            int freeSpaceWidth = this.popupX - LEFT_PANEL_WIDTH;
            centerX = LEFT_PANEL_WIDTH + (freeSpaceWidth / 2);

            // Si l'espace pour l'entité est inférieur à 120 pixels, on réduit le zoom
            if (freeSpaceWidth < 120) {
                effectiveZoom = modelZoom * (freeSpaceWidth / 120f);
            } else {
                effectiveZoom = modelZoom * 1.1f;
            }
        } else {
            int centerAreaWidth = this.width - LEFT_PANEL_WIDTH - RIGHT_PANEL_WIDTH;
            centerX = LEFT_PANEL_WIDTH + (centerAreaWidth / 2);
        }

        int centerY = this.height / 2 + 50;
        float rotY = morphologyPanelOpen ? POPUP_ROTATION_Y : modelRotationY;

        drawEntityOnScreen(centerX, centerY, effectiveZoom, rotY, modelRotationX, dummyPlayer, partialTicks);

        renderSkinOptionsMatrix(poseStack, mouseX, mouseY, rightPanelX);

        if (morphologyPanelOpen) {
            renderMorphologyPopup(poseStack);
        } else drawCenteredString(poseStack, this.font, "Clic gauche + glisser pour tourner", centerX, this.height - 80, 0x44FFFFFF);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void renderMorphologyPopup(PoseStack poseStack) {
        fill(poseStack, popupX, popupY, popupX + actualPopupW, popupY + actualPopupH, 0xDD1A1A1A);

        fill(poseStack, popupX,                      popupY,                  popupX + actualPopupW, popupY + 1,              0xFF888888);
        fill(poseStack, popupX,                      popupY + actualPopupH - 1, popupX + actualPopupW, popupY + actualPopupH,   0xFF888888);
        fill(poseStack, popupX,                      popupY,                  popupX + 1,           popupY + actualPopupH,   0xFF888888);
        fill(poseStack, popupX + actualPopupW - 1,   popupY,                  popupX + actualPopupW, popupY + actualPopupH,   0xFF888888);

        drawCenteredString(poseStack, this.font,
                I18n.get("screen.cucubanymod.character_editor.morphology.title"),
                popupX + actualPopupW / 2, popupY + 8, 0xFFFFFF);

        fill(poseStack, popupX + 8, popupY + 20, popupX + actualPopupW - 8, popupY + 21, 0x55FFFFFF);

        this.font.draw(poseStack,
                I18n.get("screen.cucubanymod.character_editor.morphology.section.body"),
                popupX + 8f, popupY + 24f, 0xAAAAAA);

        if (isBreastGender()) {
            fill(poseStack, popupX + 8, popupY + 58, popupX + actualPopupW - 8, popupY + 59, 0x55FFFFFF);
            this.font.draw(poseStack,
                    I18n.get("screen.cucubanymod.character_editor.morphology.section.bust"),
                    popupX + 8f, popupY + 62f, 0xAAAAAA);
        }
    }

    private void renderSkinOptionsMatrix(PoseStack poseStack, int mouseX, int mouseY, int panelX) {
        List<CharacterOption> options = CustomizationData.getOptionsByCategory(currentCategory);

        if (options.isEmpty()) {
            drawCenteredString(poseStack, this.font, "Aucune option", panelX + RIGHT_PANEL_WIDTH / 2, this.height / 2, 0xAAAAAA);
            return;
        }

        int startY    = 40;
        int endY      = this.height - 40;
        int viewHeight = endY - startY;

        int rows               = (int) Math.ceil((double) options.size() / ITEMS_PER_ROW);
        int totalContentHeight = rows * (ITEM_SIZE + ITEM_SPACING);

        enableScissor(panelX, startY, RIGHT_PANEL_WIDTH, viewHeight);

        int currentY = startY - (int)currentScroll;
        int currentX = panelX + 10;

        for (int i = 0; i < options.size(); i++) {
            CharacterOption option = options.get(i);
            int col = i % ITEMS_PER_ROW;
            int row = i / ITEMS_PER_ROW;
            int x   = currentX + col * (ITEM_SIZE + ITEM_SPACING);
            int y   = currentY + row * (ITEM_SIZE + ITEM_SPACING);

            if (y + ITEM_SIZE > startY && y < endY) {
                boolean isSelected = currentSelections.get(currentCategory) != null &&
                        currentSelections.get(currentCategory).id().equals(option.id());
                drawOptionPreview(poseStack, x, y, ITEM_SIZE, option, isSelected, mouseX, mouseY);
            }
        }

        RenderSystem.disableScissor();

        if (totalContentHeight > viewHeight) {
            int scrollbarX      = this.width - 6;
            int scrollbarHeight = (int) ((float)(viewHeight * viewHeight) / totalContentHeight);
            if (scrollbarHeight < 32) scrollbarHeight = 32;
            int scrollbarY = startY + (int)((currentScroll / (totalContentHeight - viewHeight)) * (viewHeight - scrollbarHeight));

            fill(poseStack, scrollbarX, startY,    scrollbarX + 4, endY,                  0xFF222222);
            fill(poseStack, scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0xFF888888);
        }
    }

    private void drawOptionPreview(PoseStack poseStack, int x, int y, int size, CharacterOption option, boolean isSelected, int mouseX, int mouseY) {
        boolean isHovered  = mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size;
        int     borderColor = isSelected ? 0xFF00FF00 : (isHovered ? 0xFFFFFFFF : 0xFF555555);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BUTTON_BG_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        blit(poseStack, x, y, size, size, 0, 0, 32, 32, 32, 32);
        fill(poseStack, x,          y,          x + size,     y + 1,        borderColor);
        fill(poseStack, x,          y + size - 1, x + size,   y + size,     borderColor);
        fill(poseStack, x,          y,          x + 1,        y + size,     borderColor);
        fill(poseStack, x + size - 1, y,        x + size,     y + size,     borderColor);

        ResourceLocation tex = ClientSkinHelper.getTextureLocation(option);
        if (tex != null) {
            int tintColor = 0xFFFFFFFF;
            if (option.category() == currentCategory && this.colorPicker.visible) {
                tintColor = this.currentTintForPreview | 0xFF000000;
            } else if (categoryColors.containsKey(option.category())) {
                tintColor = categoryColors.get(option.category()) | 0xFF000000;
            }

            float autoRotation = this.modelRotationY;

            if (option.category() == SkinPart.EYES || option.category() == SkinPart.EYEBROWS) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, tex);
                RenderSystem.enableBlend();

                int imgSize = size - 8;
                int imgX    = x + 4;
                int imgY    = y + 4;

                float r = ((tintColor >> 16) & 0xFF) / 255.0F;
                float g = ((tintColor >>  8) & 0xFF) / 255.0F;
                float b = ( tintColor        & 0xFF) / 255.0F;

                RenderSystem.setShaderColor(r, g, b, 1.0F);
                blit(poseStack, imgX, imgY, imgSize, imgSize, 0, 0, 8, 8, 16, 16);
                blit(poseStack, imgX, imgY, imgSize, imgSize, 0, 8, 8, 8, 16, 16);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                blit(poseStack, imgX, imgY, imgSize, imgSize, 8, 0, 8, 8, 16, 16);
                blit(poseStack, imgX, imgY, imgSize, imgSize, 8, 8, 8, 8, 16, 16);
                RenderSystem.disableBlend();
            } else {
                enableScissor(x + 1, y + 1, size - 2, size - 2);
                this.previewDummy.setSkin(tex);
                if (option.category() == SkinPart.BODY) {
                    this.previewDummy.setModelType(this.isSlimModel);
                } else {
                    this.previewDummy.setModelType(false);
                }
                drawPart3D(x, y, size, autoRotation, 0f, this.previewDummy, option.category().getDisplayPart(), tintColor);
                RenderSystem.disableScissor();
            }
        }

        if (isHovered) {
            renderTooltip(poseStack, new TextComponent(option.id()), mouseX, mouseY);
        }
    }

    // -------------------------------------------------------------------------
    // Interactions souris
    // -------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (morphologyPanelOpen) {
            boolean inLeftPanel = mouseX >= 0 && mouseX <= LEFT_PANEL_WIDTH;
            boolean inPopup = mouseX >= popupX && mouseX <= popupX + actualPopupW
                    && mouseY >= popupY && mouseY <= popupY + actualPopupH;

            // On autorise la fermeture si on clique n'importe où en dehors du popup et de la gauche
            if (!inLeftPanel && !inPopup) {
                morphologyPanelOpen = false;
                updatePopupWidgets();
                return true;
            }
        }

        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int rightPanelX = this.width - RIGHT_PANEL_WIDTH;

        if (mouseX >= rightPanelX && !morphologyPanelOpen) {
            List<CharacterOption> options = CustomizationData.getOptionsByCategory(currentCategory);
            int startY   = 40;
            int currentY = startY - (int)currentScroll;
            int currentX = rightPanelX + 10;

            for (int i = 0; i < options.size(); i++) {
                int col = i % ITEMS_PER_ROW;
                int row = i / ITEMS_PER_ROW;
                int x   = currentX + col * (ITEM_SIZE + ITEM_SPACING);
                int y   = currentY + row * (ITEM_SIZE + ITEM_SPACING);

                if (mouseX >= x && mouseX <= x + ITEM_SIZE && mouseY >= y && mouseY <= y + ITEM_SIZE) {
                    if (y + ITEM_SIZE > startY && y < this.height - 40) {
                        selectOption(options.get(i));
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                }
            }
        } else if (mouseX > LEFT_PANEL_WIDTH && mouseX < rightPanelX && !morphologyPanelOpen) {
            isDraggingModel = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingModel = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingModel) {
            this.modelRotationY -= dragX * 2.0f;
            this.modelRotationX += dragY * 2.0f;
            this.modelRotationX  = Mth.clamp(this.modelRotationX, -30.0f, 30.0f);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= this.width - RIGHT_PANEL_WIDTH) {
            List<CharacterOption> options     = CustomizationData.getOptionsByCategory(currentCategory);
            int rows        = (int) Math.ceil((double) options.size() / ITEMS_PER_ROW);
            int totalHeight = rows * (ITEM_SIZE + ITEM_SPACING);
            int viewHeight  = (this.height - 40) - 40;

            if (totalHeight > viewHeight) {
                currentScroll -= delta * 20f;
                currentScroll  = Mth.clamp(currentScroll, 0, totalHeight - viewHeight);
            }
            return true;
        } else if (mouseX > LEFT_PANEL_WIDTH && !morphologyPanelOpen) {
            modelZoom += delta * 2.0f;
            modelZoom  = Mth.clamp(modelZoom, 30.0f, 120.0f);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            if (morphologyPanelOpen) {
                morphologyPanelOpen = false;
                updatePopupWidgets();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // -------------------------------------------------------------------------
    // Logique métier
    // -------------------------------------------------------------------------

    private void selectOption(CharacterOption option) {
        this.currentSelections.put(currentCategory, option);
        updateDummySkin();
        playContextualAnimation(option.category());
    }

    private void playContextualAnimation(SkinPart category) {
        String animToPlay = switch (category) {
            case PANTS -> "anim_legs";
            case SHOES -> "anim_feet";
            case SHIRT, BODY -> "anim_torso";
            case HAIR, EYES, EYEBROWS, MOUTH -> "anim_head";
            default -> null;
        };
        if (animToPlay != null) {
            ClientAnimationManager.playAnimation(this.dummyPlayer, animToPlay);
        }
    }

    private void updateDummySkin() {
        List<CharacterOption> parts = new ArrayList<>(currentSelections.values());
        if (!currentSelections.containsKey(SkinPart.PANTS)) {
            CharacterOption underwear = getUnderwearOption();
            if (underwear != null) parts.add(underwear);
        }
        ResourceLocation generatedSkin = SkinManager.generateSkin(parts, this.categoryColors);
        this.dummyPlayer.setSkin(generatedSkin);
    }

    private CharacterOption getUnderwearOption() {
        if (cachedUnderwear != null) return cachedUnderwear;
        try {
            ResourceLocation loc = new ResourceLocation("cucubanymod", "textures/character/underwear.png");
            if (Minecraft.getInstance().getResourceManager().hasResource(loc)) {
                try (java.io.InputStream stream = Minecraft.getInstance().getResourceManager().getResource(loc).getInputStream()) {
                    byte[] bytes = stream.readAllBytes();
                    cachedUnderwear = new CharacterOption("default", SkinPart.PANTS, bytes);
                    return cachedUnderwear;
                }
            }
        } catch (Exception e) { /* ignore */ }
        return null;
    }

    private void sendValidationPacket() {
        Map<SkinPart, String> selectedIds = new HashMap<>();
        this.currentSelections.forEach((part, option) -> selectedIds.put(part, option.id()));
        CharacterAppearance appearance = new CharacterAppearance(
                this.isSlimModel,
                currentGender,
                this.bustSize,
                this.bustXOffset,
                this.bustYOffset,
                this.bustZOffset,
                this.bustCleavage,
                selectedIds,
                new HashMap<>(this.categoryColors)
        );
        CucubanyPacketHandler.INSTANCE.sendToServer(new SendCharacterCreationPacket(appearance));
        this.onClose();
    }

    // -------------------------------------------------------------------------
    // Rendu 3D
    // -------------------------------------------------------------------------

    private void enableScissor(int x, int y, int width, int height) {
        com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int sx = (int)(x * scale);
        int sy = (int)(window.getHeight() - (y + height) * scale);
        int sw = (int)(width  * scale);
        int sh = (int)(height * scale);
        RenderSystem.enableScissor(Math.max(0, sx), Math.max(0, sy), Math.max(0, sw), Math.max(0, sh));
    }

    public static void drawEntityOnScreen(int x, int y, float scale, float rotY, float rotX, AbstractClientPlayer player, float partialTicks) {
        player.setYBodyRot(rotY);
        player.setYHeadRot(rotY);
        player.setYRot(rotY);
        player.yHeadRotO  = rotY;
        player.yBodyRotO  = rotY;

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        poseStack.translate(x, y, 50.0D);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(rotX));

        Lighting.setupForEntityInInventory();
        dispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() ->
                dispatcher.render(player, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, poseStack, buffer, 15728880));
        buffer.endBatch();
        dispatcher.setRenderShadow(true);
        poseStack.popPose();

        Lighting.setupFor3DItems();
    }

    public void drawPart3D(int x, int y, int size, float rotY, float rotX, DummyPlayer player, SkinPart.DisplayPart partType, int tintColor) {
        player.setYBodyRot(rotY); player.setYHeadRot(rotY); player.setYRot(rotY);
        player.yBodyRotO = rotY;  player.yHeadRotO  = rotY;  player.yRotO     = rotY;

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(x + size / 2.0f, y + size / 2.0f, 250.0D);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(rotX));

        float scale = 1.0f, yOffset = 0.0f;
        switch (partType) {
            case HEAD  -> { scale = size * 1.0f;  yOffset = -1.65f; }
            case TORSO -> { scale = size * 0.65f; yOffset = -0.8f;  }
            case LEGS  -> { scale = size * 0.65f; yOffset =  0.3f;  }
            case FEET  -> { scale = size * 0.75f; yOffset =  0.8f;  }
            default    -> { scale = size * 0.45f; yOffset = -0.95f; }
        }
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, yOffset, 0);

        RenderSystem.applyModelViewMatrix();
        Lighting.setupForEntityInInventory();

        float r = ((tintColor >> 16) & 0xFF) / 255.0F;
        float g = ((tintColor >>  8) & 0xFF) / 255.0F;
        float b = ( tintColor        & 0xFF) / 255.0F;
        float a = ((tintColor >> 24) & 0xFF) / 255.0F;
        if (a == 0) a = 1.0F;

        RenderSystem.setShaderColor(r, g, b, a);
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);

        MultiBufferSource.BufferSource immediateBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        if (dispatcher.getRenderer(player) instanceof LivingEntityRenderer renderer) {
            if (renderer.getModel() instanceof PlayerModel model) {
                boolean headV  = model.head.visible;   boolean hatV     = model.hat.visible;
                boolean bodyV  = model.body.visible;   boolean jacketV  = model.jacket.visible;
                boolean rArmV  = model.rightArm.visible; boolean rSleeveV = model.rightSleeve.visible;
                boolean lArmV  = model.leftArm.visible;  boolean lSleeveV = model.leftSleeve.visible;
                boolean rLegV  = model.rightLeg.visible;  boolean rPantV   = model.rightPants.visible;
                boolean lLegV  = model.leftLeg.visible;   boolean lPantV   = model.leftPants.visible;

                model.setAllVisible(false);
                if      (partType == SkinPart.DisplayPart.HEAD)  { model.head.visible = true; model.hat.visible = true; }
                else if (partType == SkinPart.DisplayPart.TORSO) { model.body.visible = true; model.jacket.visible = true; model.rightArm.visible = true; model.rightSleeve.visible = true; model.leftArm.visible = true; model.leftSleeve.visible = true; }
                else if (partType == SkinPart.DisplayPart.LEGS)  { model.rightLeg.visible = true; model.rightPants.visible = true; model.leftLeg.visible = true; model.leftPants.visible = true; }
                else if (partType == SkinPart.DisplayPart.FEET)  { model.rightLeg.visible = true; model.rightPants.visible = true; model.leftLeg.visible = true; model.leftPants.visible = true; }
                else if (partType == SkinPart.DisplayPart.FULL)  { model.setAllVisible(true); }

                RenderSystem.runAsFancy(() -> dispatcher.render(player, 0.0D, 0.0D, 0.0D, rotY, 1.0F, poseStack, immediateBuffer, 15728880));
                immediateBuffer.endBatch();

                model.head.visible  = headV;  model.hat.visible      = hatV;
                model.body.visible  = bodyV;  model.jacket.visible   = jacketV;
                model.rightArm.visible = rArmV; model.rightSleeve.visible = rSleeveV;
                model.leftArm.visible  = lArmV; model.leftSleeve.visible  = lSleeveV;
                model.rightLeg.visible = rLegV; model.rightPants.visible  = rPantV;
                model.leftLeg.visible  = lLegV; model.leftPants.visible   = lPantV;
            }
        }
        dispatcher.setRenderShadow(true);
        poseStack.popPose();
        Lighting.setupFor3DItems();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override public boolean isPauseScreen() { return false; }
}