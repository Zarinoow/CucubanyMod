package fr.cucubany.cucubanymod.hitbox.poses;

import fr.cucubany.cucubanymod.hitbox.BodyPart;
import fr.cucubany.cucubanymod.hitbox.PartTransform;
import net.minecraft.world.entity.player.Player;

public class SneakPoseHandler implements IPartPoseHandler {

    @Override
    public PartTransform[] getTransforms(Player player, BodyPart part) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        float rad = player.yBodyRot * ((float)Math.PI / 180F);
        double lx = Math.cos(rad);
        double lz = Math.sin(rad);
        double bx = Math.sin(rad); // Vecteur Arrière
        double bz = -Math.cos(rad);

        double headY = 1.125;
        double torsoY = 0.50;
        double armY = 0.50;
        double sneakBack = 0.25;

        // Configuration Pieds/Jambes
        float footHeight = 0.15F;           // Hauteur du pied
        float legHeight = 0.65F - footHeight; // Reste de la jambe

        double footY = -0.15;                // Le pied est dans le sol
        double legY = 0;           // La jambe commence au-dessus du pied

        return switch (part) {
            case HEAD -> new PartTransform[]{ PartTransform.of(x - (bx * 0.1), y + headY, z - (bz * 0.1), 0.5F, 0.5F) };

            case TORSO -> {
                PartTransform merged = PartTransform.of(x + (bx * 0.2), y + torsoY, z + (bz * 0.2), 0.5F, 0.625F);
                yield new PartTransform[]{ merged, merged };
            }

            case ARM_LEFT -> new PartTransform[]{ PartTransform.of(x + (lx * 0.375) + (bx * 0.1), y + armY, z + (lz * 0.375) + (bz * 0.1), 0.25F, 0.60F) };
            case ARM_RIGHT -> new PartTransform[]{ PartTransform.of(x - (lx * 0.375) + (bx * 0.1), y + armY, z - (lz * 0.375) + (bz * 0.1), 0.25F, 0.60F) };

            case LEG_LEFT -> new PartTransform[]{ PartTransform.of(x + (lx * 0.125) + (bx * sneakBack), y + legY, z + (lz * 0.125) + (bz * sneakBack), 0.25F, legHeight) };
            case LEG_RIGHT -> new PartTransform[]{ PartTransform.of(x - (lx * 0.125) + (bx * sneakBack), y + legY, z - (lz * 0.125) + (bz * sneakBack), 0.25F, legHeight) };
            case FOOT_LEFT -> new PartTransform[]{ PartTransform.of(x + (lx * 0.125) + (bx * sneakBack), y + footY, z + (lz * 0.125) + (bz * sneakBack), 0.25F, footHeight) };
            case FOOT_RIGHT -> new PartTransform[]{ PartTransform.of(x - (lx * 0.125) + (bx * sneakBack), y + footY, z - (lz * 0.125) + (bz * sneakBack), 0.25F, footHeight) };
        };
    }
}