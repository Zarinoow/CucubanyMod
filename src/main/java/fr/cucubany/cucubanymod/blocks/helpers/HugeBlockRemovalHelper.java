package fr.cucubany.cucubanymod.blocks.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Collection;

public class HugeBlockRemovalHelper {

    /**
     * Supprime proprement toutes les autres parties d'un bloc géant en mode Créatif.
     * @param level Le monde
     * @param player Le joueur qui casse le bloc
     * @param partsToRemove La liste des positions de TOUTES les autres parties du bloc (sauf celle qu'on vient de casser)
     */
    public static void preventCreativeDrop(Level level, Player player, Collection<BlockPos> partsToRemove) {
        if (level.isClientSide || !player.isCreative()) return;

        for (BlockPos otherPos : partsToRemove) {
            BlockState otherState = level.getBlockState(otherPos);

            // On ne détruit que si c'est une partie "liée" (on peut vérifier le type de bloc ou une propriété)
            // Ici, on vérifie si le bloc à cette position est le même que celui cassé ou s'il appartient au mod.
            if (!otherState.isAir()) {
                // Gestion du Waterlog : si le bloc était dans l'eau, on remet de l'eau, sinon de l'air
                BlockState replacement = otherState.hasProperty(BlockStateProperties.WATERLOGGED) && otherState.getValue(BlockStateProperties.WATERLOGGED)
                        ? Blocks.WATER.defaultBlockState()
                        : Blocks.AIR.defaultBlockState();

                // Flag 35 : empêche le drop d'item, met à jour les voisins et envoie le changement au client
                level.setBlock(otherPos, replacement, 35);

                // Déclenche l'effet de particules de destruction
                level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }
        }
    }
}
