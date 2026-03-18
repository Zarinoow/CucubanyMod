package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class ClosetBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ClosetBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(addDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
        ));
    }

    /*
     * Fonctionnalité du block
     */
    abstract SkinPart[] getSkinParts();

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand hand, BlockHitResult pHit) {
        if(!hand.equals(InteractionHand.OFF_HAND)) return InteractionResult.PASS;

        pPlayer.sendMessage(new TextComponent("Ouverture du menu : " + getSkinParts().toString()), UUID.randomUUID()); // Debug

        return InteractionResult.SUCCESS;
    }

    /*
     * Création du block
     */
    public BlockState addDefaultState(BlockState bs) {
        return bs;
    }

    public void addBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        addBlockStateDefinition(pBuilder);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClosetBlockEntity(pos, state);
    }

    /*
     * Gravité
     */
    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.below()).isFaceSturdy(pLevel, pPos.below(), Direction.UP);
    }

    /*
     * Orientation
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }


}
