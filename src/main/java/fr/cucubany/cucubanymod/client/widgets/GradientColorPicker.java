package fr.cucubany.cucubanymod.client.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class GradientColorPicker extends AbstractWidget {

    private int[] colors;
    private double value = 0.0; // Entre 0.0 et 1.0
    private final Consumer<Integer> onColorChange;
    private int currentColor;

    public GradientColorPicker(int x, int y, int width, int height, int[] colors, Consumer<Integer> onColorChange) {
        super(x, y, width, height, new TextComponent(""));
        this.colors = colors;
        this.onColorChange = onColorChange;
        updateCurrentColor();
    }

    /**
     * Change les couleurs du gradient dynamiquement (ex: passer de Peau à Cheveux)
     */
    public void setColors(int... newColors) {
        this.colors = newColors;
        // On garde la position du curseur mais on recalcule la couleur résultante
        updateCurrentColor();
    }

    /**
     * Définit la valeur du slider (0.0 à 1.0)
     */
    public void setValue(double value) {
        this.value = Mth.clamp(value, 0.0, 1.0);
        updateCurrentColor();
    }

    public double getValue() {
        return this.value;
    }


    private void updateCurrentColor() {
        this.currentColor = calculateColorFromValue(this.value);
        this.onColorChange.accept(this.currentColor);
    }

    public int getCurrentColor() {
        return currentColor;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // 1. Dessiner le cadre (Bordure)
        fill(poseStack, x - 1, y - 1, x + width + 1, y + height + 1, 0xFFFFFFFF); // Bord blanc
        fill(poseStack, x, y, x + width, y + height, 0xFF000000); // Fond noir

        // 2. Dessiner le Gradient
        // Pour un rendu propre, on dessine des tranches verticales
        if (colors.length >= 2) {
            for (int i = 0; i < width; i++) {
                double progress = (double) i / (double) width;
                int col = calculateColorFromValue(progress);
                // On ajoute l'alpha opaque (FF) pour l'affichage
                fill(poseStack, x + i, y, x + i + 1, y + height, col | 0xFF000000);
            }
        }

        // 3. Dessiner le curseur
        int sliderX = x + (int) (value * (width - 2)); // -2 pour rester dans le cadre
        // Barre noire contour
        fill(poseStack, sliderX - 1, y - 2, sliderX + 3, y + height + 2, 0xFF000000);
        // Barre blanche intérieure
        fill(poseStack, sliderX, y - 1, sliderX + 2, y + height + 1, 0xFFFFFFFF);
    }

    /**
     * Logique mathématique pour mélanger les couleurs
     */
    private int calculateColorFromValue(double val) {
        if (colors.length == 0) return 0xFFFFFF;
        if (colors.length == 1) return colors[0];

        // Nombre de segments (ex: 3 couleurs = 2 segments : A->B et B->C)
        int segments = colors.length - 1;

        // Dans quel segment sommes-nous ?
        double scaledValue = val * segments;
        int index = (int) scaledValue;

        // Cas limite (valeur max)
        if (index >= segments) return colors[colors.length - 1];

        // Progression dans le segment actuel (0.0 à 1.0)
        double segmentProgress = scaledValue - index;

        int c1 = colors[index];
        int c2 = colors[index + 1];

        return interpolateColor(c1, c2, (float) segmentProgress);
    }

    private int interpolateColor(int c1, int c2, float t) {
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (r << 16) | (g << 8) | b;
    }

    // Gestion de la souris (Click & Drag)
    @Override
    public void onClick(double mouseX, double mouseY) {
        setValueFromMouse(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        setValueFromMouse(mouseX);
    }

    private void setValueFromMouse(double mouseX) {
        double relativeX = mouseX - this.x;
        this.setValue(relativeX / (double) this.width);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) { }
}