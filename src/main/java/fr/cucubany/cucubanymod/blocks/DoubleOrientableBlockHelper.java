package fr.cucubany.cucubanymod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 *
 */
public class DoubleOrientableBlockHelper {

    /*
     * Properties
     */
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;


    public static void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HALF, FACING);
    }

    /**
     * Gère la gravité : le bas doit être sur un sol solide, le haut doit être sur le bas
     * @return true si le bloc peut survivre à sa position, false sinon
     */
    public static boolean canSurvive(Block b, BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        if (pState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return pLevel.getBlockState(pPos.below()).isFaceSturdy(pLevel, pPos.below(), Direction.UP);
        } else {
            BlockState stateBelow = pLevel.getBlockState(pPos.below());
            return stateBelow.is(b) && stateBelow.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    /*
     * Block multi-niveaux
     */

    /**
     * Vérifie qu'il y a l'espace nécessaire pour poser le block du haut (Y+1) et retourne l'état du bloc à placer (avec la bonne valeur de HALF)
     * @return l'état du bloc à placer si le placement est possible, null sinon (le placement sera annulé)
     */
    public static BlockState getStateForPlacement(BlockState state, BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        // On vérifie s'il y a bien la place de poser le bloc du haut (Y+1)
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return state
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null; // Annule le placement si le plafond est trop bas
    }

    public static void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity le, ItemStack is) {
        // Dès que le bas est posé, on force l'apparition du haut
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    /**
     * Casse physiquement les différentes couches du block lorsqu'une d'entre elles est détruite.
     */
    public static BlockState updateShape(Block b, BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos) {
        DoubleBlockHalf half = pState.getValue(HALF);
        // Détruit la moitié restante si l'autre moitié est cassée
        if (pFacing.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (pFacing == Direction.UP)) {
            return pFacingState.is(b) && pFacingState.getValue(HALF) != half ? pState : Blocks.AIR.defaultBlockState();
        }
        // Gère la destruction si le bloc en dessous (le sol) disparaît
        return half == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : pState;
    }

    /**
     * Évite de rendre le modèle deux fois en ne rendant que la partie basse.
     */
    public static RenderShape getRenderShape(BlockState state) {
        // UPPER : invisible (le modèle est entièrement rendu depuis la partie basse)
        // LOWER : ENTITYBLOCK_ANIMATED → délègue au BlockEntityRenderer
        return state.getValue(HALF) == DoubleBlockHalf.UPPER
                ? RenderShape.INVISIBLE
                : RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public static boolean isMainPart(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }
}
