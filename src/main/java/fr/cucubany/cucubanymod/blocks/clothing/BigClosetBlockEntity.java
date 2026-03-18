package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.blocks.CucubanyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BigClosetBlockEntity extends ClosetBlockEntity {

    public BigClosetBlockEntity(BlockPos pos, BlockState state) {
        super(CucubanyBlockEntities.CLOSET_BIG.get(), pos, state);
    }

    /**
     * AABB étendue à 2×1×2 blocs (largeur × profondeur × hauteur) pour que
     * le frustum culling de Forge ne supprime pas le modèle quand une partie
     * sort de la AABB par défaut (1×1×1) du bloc inférieur gauche.
     *
     * La direction de la moitié latérale (OTHER) dépend du FACING du bloc.
     */
    @Override
    public AABB getRenderBoundingBox() {
        BlockPos  pos     = getBlockPos();
        BlockState state  = getBlockState();
        Direction sideDir = state.getValue(ClosetBlock.FACING).getClockWise();
        BlockPos  other   = pos.relative(sideDir);

        int minX = Math.min(pos.getX(), other.getX());
        int minZ = Math.min(pos.getZ(), other.getZ());
        int maxX = Math.max(pos.getX(), other.getX()) + 1;
        int maxZ = Math.max(pos.getZ(), other.getZ()) + 1;

        return new AABB(minX, pos.getY(), minZ, maxX, pos.getY() + 2, maxZ);
    }
}
