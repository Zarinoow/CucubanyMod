package fr.cucubany.cucubanymod.blocks;

import net.minecraft.world.phys.Vec3;

public interface IBBBlockEntity {

    /**
     * @return true si cette position est celle qui doit déclencher le rendu (ex: le bloc MASTER ou LOWER).
     */
    boolean isMainPart();

    /**
     * @return Le décalage (x, y, z) à appliquer au modèle lors du rendu.
     */
    default Vec3 getModelOffset() {
        return Vec3.ZERO;
    }

}
