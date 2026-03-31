package fr.cucubany.cucubanymod.blocks.clothing;

import fr.cucubany.cucubanymod.blocks.helpers.HugeBlockRemovalHelper;
import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BigClosetBlock extends ClosetBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    /** true = MAIN (placed by player, holds BlockEntity), false = OTHER (auto-placed partner). */
    public static final BooleanProperty PART = BooleanProperty.create("part");

    public BigClosetBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockState addDefaultState(BlockState bs) {
        return bs.setValue(HALF, DoubleBlockHalf.LOWER).setValue(PART, true);
    }

    @Override
    public void addBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, PART);
    }

    @Override
    public SkinPart[] getSkinParts() {
        return new SkinPart[]{ SkinPart.SHIRT, SkinPart.PANTS, SkinPart.SHOES };
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Direction from this block toward its lateral partner.
     * MAIN → facing.getClockWise()          (OTHER est à gauche du joueur)
     * OTHER → facing.getCounterClockWise()  (MAIN est à droite du joueur)
     *
     * "Droite du joueur" = facing.getCounterClockWise() car le joueur
     * regarde vers le bloc (direction opposée au FACING).
     */
    private static Direction getPartnerDirection(BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(PART) ? facing.getClockWise() : facing.getCounterClockWise();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Hitbox
    // ──────────────────────────────────────────────────────────────────────────

    private static int facingIndex(Direction facing) {
        switch (facing) {
            case SOUTH: return 1;
            case EAST:  return 2;
            case WEST:  return 3;
            default:    return 0; // NORTH
        }
    }

    /**
     * COLLISION_SHAPES[facingIndex][partIndex] — formes d'enceinte creuses
     * (murs, sol, plafond) utilisées pour la collision physique.
     *
     * facingIndex : N=0, S=1, E=2, W=3
     * partIndex   : MAIN=0, OTHER=1
     *
     * Chaque forme couvre Y 0-32 (2 blocs de hauteur).
     * Pour UPPER, la forme est décalée de −1 bloc vers le bas.
     *
     * Layout (facing NORTH, OTHER à l'est du MAIN) :
     *   MAIN (bloc ouest) : mur gauche + fond (sud) + sol + plafond
     *   OTHER (bloc est)  : mur droit  + fond (sud) + sol + plafond
     */
    private static final VoxelShape[][] COLLISION_SHAPES = new VoxelShape[4][2];

    /**
     * SELECTION_SHAPES[facingIndex][partIndex] — bounding box complète 2×2
     * utilisée pour l'outline de sélection (le survol met en évidence toute
     * la structure d'un coup).
     *
     * Les coordonnées peuvent dépasser 0-16 pour s'étendre sur le bloc adjacent.
     */
    private static final VoxelShape[][] SELECTION_SHAPES = new VoxelShape[4][2];

    static {
        // ── NORTH (front=−Z, back=+Z, OTHER à l'est) ─────────────────────────
        COLLISION_SHAPES[0][0] = Shapes.or(
                Block.box( 0,  0,  0,  1, 32, 16),   // mur ouest extérieur
                Block.box( 1,  0, 15, 16, 32, 16),   // fond (face sud)
                Block.box( 1,  0,  0, 16,  1, 15),   // sol
                Block.box( 1, 31,  0, 16, 32, 15)    // plafond
        );
        COLLISION_SHAPES[0][1] = Shapes.or(
                Block.box(15,  0,  0, 16, 32, 16),   // mur est extérieur
                Block.box( 0,  0, 15, 15, 32, 16),   // fond (face sud)
                Block.box( 0,  0,  0, 15,  1, 15),   // sol
                Block.box( 0, 31,  0, 15, 32, 15)    // plafond
        );
        // Enclosure creuse couvrant les 2 blocs, en coords locales du MAIN
        SELECTION_SHAPES[0][0] = Shapes.or(
                Block.box(  0,  0,  0,  1, 32, 16),   // mur ouest
                Block.box( 31,  0,  0, 32, 32, 16),   // mur est (OTHER)
                Block.box(  1,  0, 15, 31, 32, 16),   // fond
                Block.box(  1,  0,  0, 31,  1, 15),   // sol
                Block.box(  1, 31,  0, 31, 32, 15)    // plafond
        );
        SELECTION_SHAPES[0][1] = Shapes.or(
                Block.box(-16,  0,  0, -15, 32, 16),  // mur ouest (MAIN)
                Block.box( 15,  0,  0,  16, 32, 16),  // mur est
                Block.box(-15,  0, 15,  15, 32, 16),  // fond
                Block.box(-15,  0,  0,  15,  1, 15),  // sol
                Block.box(-15, 31,  0,  15, 32, 15)   // plafond
        );

        // ── SOUTH (front=+Z, back=−Z, OTHER à l'ouest) ───────────────────────
        COLLISION_SHAPES[1][0] = Shapes.or(
                Block.box(15,  0,  0, 16, 32, 16),   // mur est extérieur
                Block.box( 0,  0,  0, 15, 32,  1),   // fond (face nord)
                Block.box( 0,  0,  1, 15,  1, 16),   // sol
                Block.box( 0, 31,  1, 15, 32, 16)    // plafond
        );
        COLLISION_SHAPES[1][1] = Shapes.or(
                Block.box( 0,  0,  0,  1, 32, 16),   // mur ouest extérieur
                Block.box( 1,  0,  0, 16, 32,  1),   // fond (face nord)
                Block.box( 1,  0,  1, 16,  1, 16),   // sol
                Block.box( 1, 31,  1, 16, 32, 16)    // plafond
        );
        SELECTION_SHAPES[1][0] = Shapes.or(
                Block.box(-16,  0,  0, -15, 32, 16),  // mur ouest (OTHER)
                Block.box( 15,  0,  0,  16, 32, 16),  // mur est
                Block.box(-15,  0,  0,  15, 32,  1),  // fond (face nord)
                Block.box(-15,  0,  1,  15,  1, 16),  // sol
                Block.box(-15, 31,  1,  15, 32, 16)   // plafond
        );
        SELECTION_SHAPES[1][1] = Shapes.or(
                Block.box(  0,  0,  0,  1, 32, 16),   // mur ouest
                Block.box( 31,  0,  0, 32, 32, 16),   // mur est (MAIN)
                Block.box(  1,  0,  0, 31, 32,  1),   // fond (face nord)
                Block.box(  1,  0,  1, 31,  1, 16),   // sol
                Block.box(  1, 31,  1, 31, 32, 16)    // plafond
        );

        // ── EAST (front=+X, back=−X, OTHER au sud) ───────────────────────────
        COLLISION_SHAPES[2][0] = Shapes.or(
                Block.box( 0,  0,  0, 16, 32,  1),   // mur nord extérieur
                Block.box( 0,  0,  1,  1, 32, 16),   // fond (face ouest)
                Block.box( 1,  0,  1, 16,  1, 16),   // sol
                Block.box( 1, 31,  1, 16, 32, 16)    // plafond
        );
        COLLISION_SHAPES[2][1] = Shapes.or(
                Block.box( 0,  0, 15, 16, 32, 16),   // mur sud extérieur
                Block.box( 0,  0,  0,  1, 32, 15),   // fond (face ouest)
                Block.box( 1,  0,  0, 16,  1, 15),   // sol
                Block.box( 1, 31,  0, 16, 32, 15)    // plafond
        );
        SELECTION_SHAPES[2][0] = Shapes.or(
                Block.box( 0,  0,  0, 16, 32,  1),   // mur nord
                Block.box( 0,  0, 31, 16, 32, 32),   // mur sud (OTHER)
                Block.box( 0,  0,  1,  1, 32, 31),   // fond (face ouest)
                Block.box( 1,  0,  1, 16,  1, 31),   // sol
                Block.box( 1, 31,  1, 16, 32, 31)    // plafond
        );
        SELECTION_SHAPES[2][1] = Shapes.or(
                Block.box( 0,  0, -16, 16, 32, -15), // mur nord (MAIN)
                Block.box( 0,  0,  15, 16, 32,  16), // mur sud
                Block.box( 0,  0, -15,  1, 32,  15), // fond (face ouest)
                Block.box( 1,  0, -15, 16,  1,  15), // sol
                Block.box( 1, 31, -15, 16, 32,  15)  // plafond
        );

        // ── WEST (front=−X, back=+X, OTHER au nord) ──────────────────────────
        COLLISION_SHAPES[3][0] = Shapes.or(
                Block.box( 0,  0, 15, 16, 32, 16),   // mur sud extérieur
                Block.box(15,  0,  0, 16, 32, 15),   // fond (face est)
                Block.box( 0,  0,  0, 15,  1, 15),   // sol
                Block.box( 0, 31,  0, 15, 32, 15)    // plafond
        );
        COLLISION_SHAPES[3][1] = Shapes.or(
                Block.box( 0,  0,  0, 16, 32,  1),   // mur nord extérieur
                Block.box(15,  0,  1, 16, 32, 16),   // fond (face est)
                Block.box( 0,  0,  1, 15,  1, 16),   // sol
                Block.box( 0, 31,  1, 15, 32, 16)    // plafond
        );
        SELECTION_SHAPES[3][0] = Shapes.or(
                Block.box( 0,  0, -16, 16, 32, -15), // mur nord (OTHER)
                Block.box( 0,  0,  15, 16, 32,  16), // mur sud
                Block.box(15,  0, -15, 16, 32,  15), // fond (face est)
                Block.box( 0,  0, -15, 15,  1,  15), // sol
                Block.box( 0, 31, -15, 15, 32,  15)  // plafond
        );
        SELECTION_SHAPES[3][1] = Shapes.or(
                Block.box( 0,  0,  0, 16, 32,  1),   // mur nord
                Block.box( 0,  0, 31, 16, 32, 32),   // mur sud (MAIN)
                Block.box(15,  0,  1, 16, 32, 31),   // fond (face est)
                Block.box( 0,  0,  1, 15,  1, 31),   // sol
                Block.box( 0, 31,  1, 15, 32, 31)    // plafond
        );
    }

    /** Outline de sélection : bounding box couvrant la totalité du 2×2. */
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int fi = facingIndex(pState.getValue(FACING));
        VoxelShape sel = SELECTION_SHAPES[fi][pState.getValue(PART) ? 0 : 1];
        return pState.getValue(HALF) == DoubleBlockHalf.UPPER ? sel.move(0.0D, -1.0D, 0.0D) : sel;
    }

    /** Collision physique : formes d'enceinte creuses (murs/sol/plafond). */
    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int fi = facingIndex(pState.getValue(FACING));
        VoxelShape col = COLLISION_SHAPES[fi][pState.getValue(PART) ? 0 : 1];
        return pState.getValue(HALF) == DoubleBlockHalf.UPPER ? col.move(0.0D, -1.0D, 0.0D) : col;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Survie / gravité
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        if (pState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return pLevel.getBlockState(pPos.below()).isFaceSturdy(pLevel, pPos.below(), Direction.UP);
        } else {
            BlockState below = pLevel.getBlockState(pPos.below());
            return below.is(this)
                    && below.getValue(HALF) == DoubleBlockHalf.LOWER
                    && below.getValue(PART) == pState.getValue(PART);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Placement
    // ──────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;

        BlockPos  pos     = context.getClickedPos();
        Level     level   = context.getLevel();
        // sideDir = droite du joueur (counterClockWise du facing)
        Direction sideDir = state.getValue(FACING).getCounterClockWise();
        BlockPos  mainPos = pos.relative(sideDir);

        if (pos.getY() >= level.getMaxBuildHeight() - 1) return null;

        // Vérifier les 3 positions supplémentaires libres
        if (!level.getBlockState(pos.above()).canBeReplaced(context))               return null;
        if (!level.getBlockState(mainPos).canBeReplaced(context))                   return null;
        if (!level.getBlockState(mainPos.above()).canBeReplaced(context))           return null;

        // Vérifier que MAIN a aussi un sol solide (pas de placement à moitié dans le vide)
        if (!level.getBlockState(mainPos.below()).isFaceSturdy(level, mainPos.below(), Direction.UP)) return null;

        // Le bloc cliqué = OTHER ; MAIN (rendu) est 1 bloc à droite du joueur.
        return state.setValue(HALF, DoubleBlockHalf.LOWER).setValue(PART, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity le, ItemStack is) {
        // pos = LOWER-OTHER (bloc cliqué). MAIN se pose à droite du joueur (counterClockWise).
        Direction sideDir = state.getValue(FACING).getCounterClockWise();
        level.setBlock(pos.above(),                  state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(PART, false), 3); // UPPER-OTHER
        level.setBlock(pos.relative(sideDir),         state.setValue(HALF, DoubleBlockHalf.LOWER).setValue(PART, true),  3); // LOWER-MAIN
        level.setBlock(pos.above().relative(sideDir), state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(PART, true),  3); // UPPER-MAIN
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Destruction en cascade
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState,
                                  LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        DoubleBlockHalf half   = pState.getValue(HALF);
        boolean         isMain = pState.getValue(PART);
        Direction partnerDir   = getPartnerDirection(pState);

        // Vertical: LOWER needs matching UPPER above; UPPER needs matching LOWER below.
        if (pFacing.getAxis() == Direction.Axis.Y) {
            boolean checkUp   = (half == DoubleBlockHalf.LOWER && pFacing == Direction.UP);
            boolean checkDown = (half == DoubleBlockHalf.UPPER && pFacing == Direction.DOWN);
            if (checkUp || checkDown) {
                DoubleBlockHalf expected = checkUp ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
                if (!pFacingState.is(this)
                        || pFacingState.getValue(HALF) != expected
                        || pFacingState.getValue(PART) != isMain) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

        // Lateral: each half needs its partner (opposite PART, same HALF) next to it.
        if (pFacing == partnerDir) {
            if (!pFacingState.is(this)
                    || pFacingState.getValue(PART) == isMain   // partner must differ
                    || pFacingState.getValue(HALF) != half) {
                return Blocks.AIR.defaultBlockState();
            }
        }

        // Ground: LOWER blocks need a sturdy surface below.
        if (half == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN
                && !pState.canSurvive(pLevel, pCurrentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (!pLevel.isClientSide && pPlayer.isCreative()) {
            boolean isMain = pState.getValue(PART);
            DoubleBlockHalf half = pState.getValue(HALF);
            Direction facing = pState.getValue(FACING);

            // 1. Trouver la position absolue du "Pivot" (MAIN + LOWER)
            BlockPos mainLowerPos = pPos;

            // Si on a cassé un bloc du haut, on descend pour trouver le bas
            if (half == DoubleBlockHalf.UPPER) {
                mainLowerPos = mainLowerPos.below();
            }

            // Si on a cassé la partie OTHER, on se décale vers le MAIN (à droite du joueur)
            if (!isMain) {
                mainLowerPos = mainLowerPos.relative(facing.getCounterClockWise());
            }

            // 2. Calculer les 4 positions occupées par le placard complet
            Direction sideDir = facing.getClockWise(); // Direction vers la partie OTHER

            List<BlockPos> allParts = List.of(
                    mainLowerPos,                          // MAIN LOWER
                    mainLowerPos.above(),                  // MAIN UPPER
                    mainLowerPos.relative(sideDir),        // OTHER LOWER
                    mainLowerPos.relative(sideDir).above() // OTHER UPPER
            );

            // 3. Filtrer pour obtenir uniquement les "autres" parties (et vérifier qu'elles sont bien là)
            List<BlockPos> others = allParts.stream()
                    .filter(pos -> !pos.equals(pPos)) // On exclut le bloc qu'on est déjà en train de casser
                    .filter(pos -> pLevel.getBlockState(pos).is(this)) // Sécurité
                    .toList();

            // 4. Supprimer silencieusement les autres parties
            HugeBlockRemovalHelper.preventCreativeDrop(pLevel, pPlayer, others);
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Rendu
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Only LOWER+MAIN is rendered via the BlockEntityRenderer; everything else is invisible.
        return (state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(PART))
                ? RenderShape.ENTITYBLOCK_ANIMATED
                : RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return (state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(PART))
                ? new BigClosetBlockEntity(pos, state)
                : null;
    }
}
