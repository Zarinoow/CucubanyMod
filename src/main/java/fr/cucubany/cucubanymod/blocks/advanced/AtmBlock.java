package fr.cucubany.cucubanymod.blocks.advanced;

import fr.cucubany.cucubanymod.blocks.DoubleOrientableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import static fr.cucubany.cucubanymod.blocks.DoubleOrientableBlockHelper.HALF;

public class AtmBlock extends DoubleOrientableBlock implements EntityBlock {

    public AtmBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // On ne crée une BlockEntity que pour la partie basse (celle qui est rendue).
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? new AtmBlockEntity(pos, state)
                : null;
    }
}
