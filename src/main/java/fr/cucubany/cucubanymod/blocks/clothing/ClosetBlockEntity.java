package fr.cucubany.cucubanymod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ClosetBlockEntity extends BlockEntity {

    public ClosetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(CucubanyBlockEntities.CLOSET.get(), pos, state);
    }

}
