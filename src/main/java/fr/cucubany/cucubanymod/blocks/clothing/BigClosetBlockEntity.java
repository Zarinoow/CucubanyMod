package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.blocks.CucubanyBlockEntities;
import fr.cucubany.cucubanymod.blocks.HugeBlockAABBHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static fr.cucubany.cucubanymod.blocks.clothing.BigClosetBlock.HALF;
import static fr.cucubany.cucubanymod.blocks.clothing.ClosetBlock.FACING;

public class BigClosetBlockEntity extends ClosetBlockEntity {

    public BigClosetBlockEntity(BlockPos pos, BlockState state) {
        super(CucubanyBlockEntities.CLOSET_BIG.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return HugeBlockAABBHelper.getOrientedBox(getBlockPos(), getBlockState().getValue(ClosetBlock.FACING), 2, 2, 1);
    }

    @Override
    public boolean isMainPart() {
        return this.getBlockState().getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public Vec3 getModelOffset() {
        Direction facing = this.getBlockState().getValue(FACING);
        Direction left = facing.getClockWise();
        // On décale de 1 bloc vers la gauche du facing
        return new Vec3(left.getStepX(), 0, left.getStepZ());
    }
}
