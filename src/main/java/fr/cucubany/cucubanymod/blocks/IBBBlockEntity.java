package fr.cucubany.cucubanymod.blocks;

import net.minecraft.world.phys.Vec3;

public interface IBBBlockEntity {

    enum BBRenderType {
        /** Rendu avec backface culling : évite le z-fighting sur les éléments plats visibles en façade. */
        BB_CUTOUT,
        /** Rendu sans backface culling : pour les modèles avec géométrie visible des deux côtés. */
        BB_CUTOUT_NOCULL
    }

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

    /**
     * @return Le type de rendu à utiliser. Par défaut BB_CUTOUT_NOCULL (double face, pour les
     *         modèles avec géométrie visible des deux côtés). Utiliser BB_CUTOUT pour les modèles
     *         avec des éléments plats visibles en façade (évite le z-fighting).
     */
    default BBRenderType getBBRenderType() {
        return BBRenderType.BB_CUTOUT_NOCULL;
    }

}
