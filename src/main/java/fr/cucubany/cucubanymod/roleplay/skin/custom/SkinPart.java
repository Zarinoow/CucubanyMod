package fr.cucubany.cucubanymod.roleplay.skin.custom;

public enum SkinPart {

    BODY(DisplayPart.FULL),
    HAIR(DisplayPart.HEAD),
    EYES(DisplayPart.HEAD),
    EYEBROWS(DisplayPart.HEAD),
    MOUTH(DisplayPart.HEAD),
    SHIRT(DisplayPart.TORSO),
    PANTS(DisplayPart.LEGS),
    SHOES(DisplayPart.FEET);

    private final DisplayPart displayPart;

    SkinPart(DisplayPart displayPart) {
        this.displayPart = displayPart;
    }

    public DisplayPart getDisplayPart() {
        return displayPart;
    }

    public enum DisplayPart {
        HEAD, TORSO, LEGS, FEET, FULL;
    }
}
