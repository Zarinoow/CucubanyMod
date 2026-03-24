package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.blocks.CucubanyBlockEntities;
import fr.cucubany.cucubanymod.blocks.helpers.DoubleOrientableBlockHelper;
import fr.cucubany.cucubanymod.blocks.helpers.HugeBlockAABBHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class DoubleClosetBlockEntity extends ClosetBlockEntity {

    public DoubleClosetBlockEntity(BlockPos pos, BlockState state) {
        super(CucubanyBlockEntities.CLOSET_DOUBLE.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return HugeBlockAABBHelper.getTallBox(getBlockPos(), 2);
    }

    @Override
    public boolean isMainPart() {
        return DoubleOrientableBlockHelper.isMainPart(getBlockState());
    }
}