package fr.cucubany.cucubanymod.hitbox;

public enum BodyPart {
    // SYNTAXE :
    // Nom, Count, R, G, B,
    // GuiX, GuiY, GuiW, GuiH, (Position sur votre overlay 128x128)
    // SkinU, SkinV, SkinW, SkinH (Position sur le skin Minecraft 64x64)

    HEAD("head", 1, 1.0F, 0.0F, 0.0F,
            5, 0, 10, 10,        // GUI
            8, 8, 8, 8),         // SKIN

    TORSO("torso", 2, 1.0F, 1.0F, 0.0F,
            5, 10, 10, 14,       // GUI
            20, 20, 8, 12),      // SKIN

    ARM_LEFT("arm_left", 1, 0.0F, 0.0F, 1.0F,
            15, 10, 5, 14,       // GUI
            36, 52, 3, 12),         // SKIN

    ARM_RIGHT("arm_right", 1, 0.0F, 0.0F, 1.0F,
            0, 10, 5, 14,        // GUI
            44, 20, 3, 12),         // SKIN

    LEG_LEFT("leg_left", 1, 0.0F, 1.0F, 0.0F,
            10, 24, 6, 10,       // GUI
            4, 20, 4, 8),        // SKIN

    LEG_RIGHT("leg_right", 1, 0.0F, 1.0F, 0.0F,
            4, 24, 6, 10,        // GUI
            20, 52, 4, 8),       // SKIN

    FOOT_LEFT("foot_left", 1, 0.0F, 1.0F, 1.0F,
            10, 34, 6, 6,        // GUI
            4, 28, 4, 4),        // SKIN

    FOOT_RIGHT("foot_right", 1, 0.0F, 1.0F, 1.0F,
            4, 34, 6, 6,         // GUI
            20, 60, 4, 4);       // SKIN

    private final String name;
    private final int partCount;
    private final float r, g, b;

    // Coordonnées GUI (Offset dans le bonhomme de 20x40)
    private final int guiX, guiY, guiWidth, guiHeight;

    // Coordonnées SKIN (Texture 64x64 Minecraft)
    private final int skinU, skinV, skinWidth, skinHeight;

    BodyPart(String name, int partCount, float r, float g, float b,
             int guiX, int guiY, int guiW, int guiH,
             int skinU, int skinV, int skinW, int skinH) {
        this.name = name;
        this.partCount = partCount;
        this.r = r;
        this.g = g;
        this.b = b;
        this.guiX = guiX;
        this.guiY = guiY;
        this.guiWidth = guiW;
        this.guiHeight = guiH;
        this.skinU = skinU;
        this.skinV = skinV;
        this.skinWidth = skinW;
        this.skinHeight = skinH;
    }

    public String getName() { return name; }
    public int getPartCount() { return partCount; }
    public float getRed() { return r; }
    public float getGreen() { return g; }
    public float getBlue() { return b; }

    // Getters GUI
    public int getGuiX() { return guiX; }
    public int getGuiY() { return guiY; }
    public int getGuiWidth() { return guiWidth; }
    public int getGuiHeight() { return guiHeight; }

    // Getters SKIN
    public int getSkinU() { return skinU; }
    public int getSkinV() { return skinV; }
    public int getSkinWidth() { return skinWidth; }
    public int getSkinHeight() { return skinHeight; }
}