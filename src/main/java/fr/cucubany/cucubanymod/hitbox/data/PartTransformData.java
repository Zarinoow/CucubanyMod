package fr.cucubany.cucubanymod.hitbox.data;

public class PartTransformData {
    public double lateralOffset;
    public double backwardOffset;
    public double yOffset;
    public float width;
    public float height;

    public PartTransformData(double lateralOffset, double backwardOffset, double yOffset, float width, float height) {
        this.lateralOffset = lateralOffset;
        this.backwardOffset = backwardOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
    }

    public PartTransformData deepCopy() {
        return new PartTransformData(lateralOffset, backwardOffset, yOffset, width, height);
    }
}
