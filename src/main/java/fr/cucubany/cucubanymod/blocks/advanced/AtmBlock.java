package fr.cucubany.cucubanymod.blocks.advanced;

import fr.cucubany.cucubanymod.blocks.DoubleOrientableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AtmBlock extends DoubleOrientableBlock implements EntityBlock {


    public AtmBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return null;
    }
}
