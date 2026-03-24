package fr.cucubany.cucubanymod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class HugeBlockAABBHelper {

    /**
     * Méthode de base : Crée une boîte à partir de dimensions brutes (X, Y, Z).
     * @param xSize Nombre de blocs en X (Est/Ouest)
     * @param ySize Nombre de blocs en Y (Hauteur)
     * @param zSize Nombre de blocs en Z (Nord/Sud)
     */
    public static AABB getAbsoluteBox(BlockPos pos, int xSize, int ySize, int zSize) {
        return new AABB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + xSize, pos.getY() + ySize, pos.getZ() + zSize
        );
    }

    /**
     * Spécifique pour les blocs verticaux (Poteaux, Portes, etc.)
     * 1 block de largeur et de profondeur, mais une hauteur personnalisée.
     */
    public static AABB getTallBox(BlockPos pos, int height) {
        return getAbsoluteBox(pos, 1, height, 1);
    }

    /**
     * Calcule la boîte en fonction du facing du bloc.
     * @param facing La direction du bloc
     * @param width  Largeur (sur le côté du facing)
     * @param height Hauteur (Y)
     * @param depth  Profondeur (vers l'arrière du facing)
     */
    public static AABB getOrientedBox(BlockPos pos, Direction facing, int width, int height, int depth) {
        Direction sideDir = facing.getClockWise();
        Direction backDir = facing.getOpposite();

        // On calcule le point opposé en se déplaçant depuis l'origine
        BlockPos farCorner = pos
                .relative(sideDir, width - 1)
                .relative(backDir, depth - 1);

        return new AABB(
                Math.min(pos.getX(), farCorner.getX()),
                pos.getY(),
                Math.min(pos.getZ(), farCorner.getZ()),
                Math.max(pos.getX(), farCorner.getX()) + 1.0D,
                pos.getY() + height,
                Math.max(pos.getZ(), farCorner.getZ()) + 1.0D
        );
    }
}