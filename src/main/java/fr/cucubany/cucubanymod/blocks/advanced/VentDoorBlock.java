package fr.cucubany.cucubanymod.blocks.advanced;

import fr.cucubany.cucubanymod.items.CucubanyItems;
import fr.cucubany.cucubanymod.sounds.CucubanySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class VentDoorBlock extends Block {

    public static final EnumProperty<VentDoorState> STATE = EnumProperty.create("state", VentDoorState.class);
    public static final EnumProperty<VentDoorAttachment> ATTACHMENT = EnumProperty.create("attachment", VentDoorAttachment.class);
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

    public VentDoorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, VentDoorAttachment.WALL).setValue(STATE, VentDoorState.CLOSED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, ATTACHMENT, STATE);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem().equals(CucubanyItems.SCREWDRIVER.get())) {
            VentDoorState oldState = state.getValue(STATE);
            VentDoorState newState = oldState == VentDoorState.CLOSED ? VentDoorState.OPEN : VentDoorState.CLOSED;
            level.setBlock(pos, state.setValue(STATE, newState), 3);

            level.playSound(null, pos, CucubanySounds.VENT_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);

            heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VentDoorState doorState = state.getValue(STATE);

        switch (state.getValue(ATTACHMENT)) {
            case WALL -> {
                if (doorState == VentDoorState.OPEN) {
                    return Shapes.box(0, 0, 0, 1, 0.0625f, 1);
                }
            }
            case FLOOR -> {
                if (doorState == VentDoorState.CLOSED) {
                    return Shapes.box(0, 0, 0, 1, 0.0625f, 1);
                }
            }
            case CEILING -> {
                if (doorState == VentDoorState.CLOSED) {
                    return Shapes.box(0, 0.9375f, 0, 1, 1, 1);
                }
            }
        }

        switch (state.getValue(FACING)) {
            case NORTH -> {
                return Shapes.box(0, 0, 0.9375f, 1, 1, 1);
            }
            case SOUTH -> {
                return Shapes.box(0, 0, 0, 1, 1, 0.0625f);
            }
            case EAST -> {
                return Shapes.box(0, 0, 0, 0.0625f, 1, 1);
            }
            case WEST -> {
                return Shapes.box(0.9375f, 0, 0, 1, 1, 1);
            }
        }

        return Shapes.box(0, 0, 0, 1, 1, 1);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        VentDoorAttachment attachment;

        switch (clickedFace) {
            case UP:
                attachment = VentDoorAttachment.FLOOR;
                break;
            case DOWN:
                attachment = VentDoorAttachment.CEILING;
                break;
            default:
                attachment = VentDoorAttachment.WALL;
                break;
        }

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ATTACHMENT, attachment);
    }
}

enum VentDoorState implements StringRepresentable {
    OPEN,
    CLOSED;

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}

enum VentDoorAttachment implements StringRepresentable {
    CEILING,
    FLOOR,
    WALL;

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
