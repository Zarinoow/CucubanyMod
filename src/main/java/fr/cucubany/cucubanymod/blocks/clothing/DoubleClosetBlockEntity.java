package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.blocks.ClosetBlockEntity;
import fr.cucubany.cucubanymod.blocks.CucubanyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ClassicClosetBlockEntity extends ClosetBlockEntity {

    public ClassicClosetBlockEntity(BlockPos pos, BlockState state) {
        super(CucubanyBlockEntities.CLOSET.get(), pos, state);
    }

    /**
     * AABB étendue à 2 blocs de hauteur pour que le frustum culling de Forge
     * ne supprime pas le bloc quand on lève la tête (la partie haute dépasse
     * la AABB de 1 bloc par défaut basée sur le bloc inférieur).
     */
    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }
}