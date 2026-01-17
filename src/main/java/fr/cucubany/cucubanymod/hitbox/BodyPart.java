package fr.cucubany.cucubanymod.hitbox;

import java.awt.Color;

public enum BodyPart {
    HEAD("head", 1, 1.0F, 0.0F, 0.0F),
    TORSO("torso", 2, 1.0F, 1.0F, 0.0F),
    ARM_LEFT("arm_left", 1, 0.0F, 0.0F, 1.0F),
    ARM_RIGHT("arm_right", 1, 0.0F, 0.0F, 1.0F),
    LEG_LEFT("leg_left", 1, 0.0F, 1.0F, 0.0F),
    LEG_RIGHT("leg_right", 1, 0.0F, 1.0F, 0.0F);

    private final String name;
    private final int partCount;
    private final float r, g, b;

    BodyPart(String name, int partCount, float r, float g, float b) {
        this.name = name;
        this.partCount = partCount;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public String getName() { return name; }
    public int getPartCount() { return partCount; }
    public float getRed() { return r; }
    public float getGreen() { return g; }
    public float getBlue() { return b; }
}