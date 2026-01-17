package fr.cucubany.cucubanymod.hitbox;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;

public record PartTransform(Vec3 position, EntityDimensions size) {
    public static PartTransform of(double x, double y, double z, float width, float height) {
        return new PartTransform(new Vec3(x, y, z), EntityDimensions.scalable(width, height));
    }
}