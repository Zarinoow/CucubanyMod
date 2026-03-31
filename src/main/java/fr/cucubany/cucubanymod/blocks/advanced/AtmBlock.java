package fr.cucubany.cucubanymod.blocks.advanced;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import fr.cucubany.cucubanymod.blocks.DoubleOrientableBlock;
import fr.cucubany.cucubanymod.blocks.helpers.DoubleOrientableBlockHelper;
import fr.cucubany.cucubanymod.client.screen.ATMScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static fr.cucubany.cucubanymod.blocks.helpers.DoubleOrientableBlockHelper.HALF;

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

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pHand.equals(InteractionHand.MAIN_HAND)) return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        // TEMP TEMP TEMP TEMP //
        if(!pLevel.isClientSide) return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ATMScreen(pPos));
        return InteractionResult.SUCCESS;
    }
}
