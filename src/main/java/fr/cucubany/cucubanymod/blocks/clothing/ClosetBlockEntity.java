package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.blocks.CucubanyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ClosetBlockEntity extends BlockEntity {

    public ClosetBlockEntity(BlockPos pos, BlockState state) {
        super(CucubanyBlockEntities.CLOSET_SIMPLE.get(), pos, state);
    }

    public ClosetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

}
