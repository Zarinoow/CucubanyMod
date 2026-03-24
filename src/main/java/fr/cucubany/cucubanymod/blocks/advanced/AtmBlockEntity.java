package fr.cucubany.cucubanymod.blocks.advanced;

import fr.cucubany.cucubanymod.blocks.CucubanyBlockEntities;
import fr.cucubany.cucubanymod.blocks.DoubleOrientableBlockHelper;
import fr.cucubany.cucubanymod.blocks.HugeBlockAABBHelper;
import fr.cucubany.cucubanymod.blocks.IBBBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

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
}
