package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ClassicClosetBlock extends ClosetBlock {

    // Propriétés du bloc
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public ClassicClosetBlock(Properties pProperties) {
        super(pProperties);
    }

    /*
     * Méthodes héritées de ClosetBlock
     */
    @Override
    public BlockState addDefaultState(BlockState bs) {
        return bs.setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void addBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    public SkinPart[] getSkinParts() {
        return new SkinPart[]{
                SkinPart.SHIRT, SkinPart.PANTS
        };
    }

    /*
     * Hitbox
     */

    /**
     * Index :
     * 0 -> NORTH
     * 1 -> SOUTH
     * 2 -> EAST
     * 3 -> WEST
     */
    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            /*
             * Gauche
             * Droit
             * Fond
             * Sol
             * Plafond
             */
            Shapes.or(
                    Block.box(0, 0, 0, 1, 32, 16),
                    Block.box(15, 0, 0, 16, 32, 16),
                    Block.box(1, 0, 15, 15, 32, 16),
                    Block.box(1, 0, 0, 15, 1, 15),
                    Block.box(1, 31, 0, 15, 32, 15)
            ),
            Shapes.or(
                    Block.box(0, 0, 0, 1, 32, 16),
                    Block.box(15, 0, 0, 16, 32, 16),
                    Block.box(1, 0, 0, 15, 32, 1),
                    Block.box(1, 0, 1, 15, 1, 16),
                    Block.box(1, 31, 1, 15, 32, 16)
            ),
            Shapes.or(
                    Block.box(0, 0, 0, 16, 32, 1),
                    Block.box(0, 0, 15, 16, 32, 16),
                    Block.box(0, 0, 1, 1, 32, 15),
                    Block.box(1, 0, 1, 16, 1, 15),
                    Block.box(1, 31, 1, 16, 32, 15)
            ),
            Shapes.or(
                    Block.box(0, 0, 0, 16, 32, 1),
                    Block.box(0, 0, 15, 16, 32, 16),
                    Block.box(15, 0, 1, 16, 32, 15),
                    Block.box(0, 0, 1, 15, 1, 15),
                    Block.box(0, 31, 1, 15, 32, 15)
            )
    };

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        // 1. On récupère la bonne forme selon la direction
        Direction facing = pState.getValue(FACING); // Hérité de ClosetBlock
        VoxelShape currentShape;

        switch (facing) {
            case SOUTH: currentShape = SHAPES[1]; break;
            case EAST:  currentShape = SHAPES[2];  break;
            case WEST:  currentShape = SHAPES[3];  break;
            default:    currentShape = SHAPES[0]; break;
        }

        // 2. Si on est sur le bloc du HAUT, on décale la hitbox de 1 bloc complet vers le bas (Y - 1)
        if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return currentShape.move(0.0D, -1.0D, 0.0D);
        }

        // Sinon, c'est le bloc du bas, on retourne la forme normale
        return currentShape;
    }

    /*
     * Gravité
     */
    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        // Gère la gravité : le bas doit être sur un sol solide, le haut doit être sur le bas
        if (pState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return pLevel.getBlockState(pPos.below()).isFaceSturdy(pLevel, pPos.below(), Direction.UP);
        } else {
            BlockState stateBelow = pLevel.getBlockState(pPos.below());
            return stateBelow.is(this) && stateBelow.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    /*
     * Block multi-niveaux
     */

    /**
     * Vérifie qu'il y a l'espace nécessaire pour poser le block
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        // On vérifie s'il y a bien la place de poser le bloc du haut (Y+1)
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return state
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null; // Annule le placement si le plafond est trop bas
    }

    /**
     * Construit physiquement les différentes couches du block
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity le, ItemStack is) {
        // Dès que le bas est posé, on force l'apparition du haut
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    /**
     * Casse physiquement les différentes couches du block lorsqu'une d'entre elles est détruite.
     */
    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        DoubleBlockHalf half = pState.getValue(HALF);
        // Détruit la moitié restante si l'autre moitié est cassée
        if (pFacing.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (pFacing == Direction.UP)) {
            return pFacingState.is(this) && pFacingState.getValue(HALF) != half ? pState : Blocks.AIR.defaultBlockState();
        }
        // Gère la destruction si le bloc en dessous (le sol) disparaît
        return half == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    /**
     * Évite de rendre le modèle deux fois en ne rendant que la partie basse.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        // UPPER : invisible (le modèle est entièrement rendu depuis la partie basse)
        // LOWER : ENTITYBLOCK_ANIMATED → délègue au BlockEntityRenderer (ClosetBlockEntityRenderer)
        return state.getValue(HALF) == DoubleBlockHalf.UPPER
                ? RenderShape.INVISIBLE
                : RenderShape.ENTITYBLOCK_ANIMATED;
    }

    /**
     * Permet de réaliser le rendu du block à l'aide du BER
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // On ne crée une BlockEntity que pour la partie basse (celle qui est rendue).
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? new DoubleClosetBlockEntity(pos, state)
                : null;
    }


}