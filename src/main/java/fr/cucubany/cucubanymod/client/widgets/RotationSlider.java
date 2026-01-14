package fr.cucubany.cucubanymod.client.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class RotationSlider extends AbstractSliderButton {
    private final java.util.function.Consumer<Float> onChange;

    public RotationSlider(int x, int y, int width, int height, float initialValue, java.util.function.Consumer<Float> onChange) {
        super(x, y, width, height, new TextComponent(""), initialValue);
        this.onChange = onChange;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        int angle = (int) ((this.value * 360.0) - 180.0); // Affichage en [-180, 180]
        this.setMessage(new TranslatableComponent("screen.cucubanymod.character_editor.button.rotation", angle));
    }


    @Override
    protected void applyValue() {
        float angle = (float) (this.value * 360.0) - 180.0f; // Transformation vers [-180, 180]
        onChange.accept(angle); // Appliquer cette valeur
    }


}
