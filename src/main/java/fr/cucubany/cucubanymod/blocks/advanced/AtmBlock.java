package fr.cucubany.cucubanymod.blocks.advanced;

import fr.cucubany.cucubanymod.blocks.DoubleOrientableBlock;
import fr.cucubany.cucubanymod.blocks.DoubleOrientableBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static fr.cucubany.cucubanymod.blocks.DoubleOrientableBlockHelper.HALF;

public class AtmBlock extends DoubleOrientableBlock implements EntityBlock {

    /**
     * Index :
     * 0 -> NORTH
     * 1 -> SOUTH
     * 2 -> WEST
     * 3 -> EAST
     */
    private static final VoxelShape[] SHAPES = {
            Block.box(3,   0, 7, 13, 26, 16), // NORTH
            Block.box(3,   0, 0, 13, 26,  9), // SOUTH
            Block.box(7,   0, 3, 16, 26, 13), // WEST
            Block.box(0,   0, 3,  9, 26, 13) // EAST
    };

    public AtmBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? new AtmBlockEntity(pos, state)
                : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return DoubleOrientableBlockHelper.getShape(SHAPES, state);
    }
}
