package fr.cucubany.cucubanymod.roleplay.skin.custom;

public enum SkinPart {

    // renderPriority : ordre de composition (plus petit = calque du bas, plus grand = calque du dessus)
    BODY    (DisplayPart.FULL,   0),
    HAIR    (DisplayPart.HEAD,  70),
    EYES    (DisplayPart.HEAD,  10),
    EYEBROWS(DisplayPart.HEAD,  30),
    MOUTH   (DisplayPart.HEAD,  20),
    SHIRT   (DisplayPart.TORSO, 40),
    PANTS   (DisplayPart.LEGS,  50),
    SHOES   (DisplayPart.FEET,  60);


    private final DisplayPart displayPart;
    private final int renderPriority;

    SkinPart(DisplayPart displayPart, int renderPriority) {
        this.displayPart = displayPart;
        this.renderPriority = renderPriority;
    }

    public DisplayPart getDisplayPart() {
        return displayPart;
    }

    /** Ordre de composition des calques : valeur faible = en dessous, valeur élevée = par-dessus. */
    public int getRenderPriority() {
        return renderPriority;
    }

    public enum DisplayPart {
        HEAD, TORSO, LEGS, FEET, FULL
    }
}
