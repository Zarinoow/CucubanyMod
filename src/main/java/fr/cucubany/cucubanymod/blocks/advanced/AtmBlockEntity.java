package fr.cucubany.cucubanymod.blocks.advanced;

import fr.cucubany.cucubanymod.blocks.CucubanyBlockEntities;
import fr.cucubany.cucubanymod.blocks.DoubleOrientableBlockHelper;
import fr.cucubany.cucubanymod.blocks.HugeBlockAABBHelper;
import fr.cucubany.cucubanymod.blocks.IBBBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AtmBlockEntity extends BlockEntity implements IBBBlockEntity {

    public AtmBlockEntity(BlockPos pos, BlockState state) {
        super(CucubanyBlockEntities.ATM.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return HugeBlockAABBHelper.getTallBox(getBlockPos(), 2);
    }

    @Override
    public boolean isMainPart() {
        return DoubleOrientableBlockHelper.isMainPart(getBlockState());
    }

    @Override
    public BBRenderType getBBRenderType() {
        return BBRenderType.BB_CUTOUT;
    }

    /**
     * Décale le modèle de 3 pixels vers l'arrière (contre le mur), selon l'orientation.
     */
    @Override
    public Vec3 getModelOffset() {
        Direction facing = getBlockState().getValue(DoubleOrientableBlockHelper.FACING);
        double pixels = 0.17375; // 2.75 / 16.0 (= 2.75 pixel en arrière)
        return new Vec3(-facing.getStepX() * pixels, 0, -facing.getStepZ() * pixels);
    }
}
