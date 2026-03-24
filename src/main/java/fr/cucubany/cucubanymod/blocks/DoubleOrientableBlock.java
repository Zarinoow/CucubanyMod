package fr.cucubany.cucubanymod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import static fr.cucubany.cucubanymod.blocks.DoubleOrientableBlockHelper.FACING;
import static fr.cucubany.cucubanymod.blocks.DoubleOrientableBlockHelper.HALF;

public abstract class DoubleOrientableBlock extends Block {

    public DoubleOrientableBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        DoubleOrientableBlockHelper.createBlockStateDefinition(pBuilder);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return DoubleOrientableBlockHelper.canSurvive(this, pState, pLevel, pPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return DoubleOrientableBlockHelper.getStateForPlacement(super.getStateForPlacement(context), context);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        DoubleOrientableBlockHelper.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        return DoubleOrientableBlockHelper.updateShape(this, pState, pDirection, pNeighborState, pLevel, pCurrentPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return DoubleOrientableBlockHelper.getRenderShape(pState);
    }
}
