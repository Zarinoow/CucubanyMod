package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.blocks.helpers.DoubleOrientableBlockHelper;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static fr.cucubany.cucubanymod.blocks.helpers.DoubleOrientableBlockHelper.HALF;

public class ClassicClosetBlock extends ClosetBlock {

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
     * 2 -> WEST
     * 3 -> EAST
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
                    Block.box(15, 0, 1, 16, 32, 15),
                    Block.box(0, 0, 1, 15, 1, 15),
                    Block.box(0, 31, 1, 15, 32, 15)
            ),
            Shapes.or(
                    Block.box(0, 0, 0, 16, 32, 1),
                    Block.box(0, 0, 15, 16, 32, 16),
                    Block.box(0, 0, 1, 1, 32, 15),
                    Block.box(1, 0, 1, 16, 1, 15),
                    Block.box(1, 31, 1, 16, 32, 15)
            )
    };

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return DoubleOrientableBlockHelper.getShape(SHAPES, pState);
    }

    /*
     * Block multi-niveaux
     */

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return DoubleOrientableBlockHelper.canSurvive(this, pState, pLevel, pPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return DoubleOrientableBlockHelper.getStateForPlacement(this.defaultBlockState(), context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity le, ItemStack is) {
        DoubleOrientableBlockHelper.setPlacedBy(level, pos, state, le, is);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return DoubleOrientableBlockHelper.updateShape(this, pState, pFacing, pFacingState, pLevel, pCurrentPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return DoubleOrientableBlockHelper.getRenderShape(state);
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