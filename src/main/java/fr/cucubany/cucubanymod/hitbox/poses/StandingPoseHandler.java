package fr.cucubany.cucubanymod.hitbox.poses;

import fr.cucubany.cucubanymod.hitbox.BodyPart;
import fr.cucubany.cucubanymod.hitbox.PartTransform;
import net.minecraft.world.entity.player.Player;

public class StandingPoseHandler implements IPartPoseHandler {

    @Override
    public PartTransform[] getTransforms(Player player, BodyPart part) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        float rad = player.yBodyRot * ((float)Math.PI / 180F);
        double lx = Math.cos(rad);
        double lz = Math.sin(rad);

        // --- HAUTEURS ---
        double headY = 1.375;
        double torsoY = 0.75;
        double armY = 0.65625;

        // Configuration Pieds/Jambes
        float footHeight = 0.15F;           // Hauteur du pied
        float legHeight = 0.75F - footHeight; // Reste de la jambe (0.60)

        double footY = 0.0;                 // Le pied touche le sol
        double legY = footHeight;           // La jambe commence au-dessus du pied

        // --- DISTANCES ---
        double torsoDist = 0.125;
        double legDist = 0.125;
        double armDist = 0.375;

        return switch (part) {
            // --- HAUT DU CORPS (INTACT) ---
            case HEAD -> new PartTransform[]{
                    PartTransform.of(x, y + headY, z, 0.5F, 0.5F)
            };

            case TORSO -> new PartTransform[]{
                    PartTransform.of(x + (lx * torsoDist), y + torsoY, z + (lz * torsoDist), 0.25F, 0.65F),
                    PartTransform.of(x - (lx * torsoDist), y + torsoY, z - (lz * torsoDist), 0.25F, 0.65F)
            };

            case ARM_LEFT -> new PartTransform[]{ PartTransform.of(x + (lx * armDist), y + armY, z + (lz * armDist), 0.25F, 0.75F) };
            case ARM_RIGHT -> new PartTransform[]{ PartTransform.of(x - (lx * armDist), y + armY, z - (lz * armDist), 0.25F, 0.75F) };

            // --- BAS DU CORPS (MODIFIÉ) ---

            // JAMBES : On les remonte à "legY" (0.15) et on réduit leur taille à "legHeight" (0.60)
            case LEG_LEFT -> new PartTransform[]{ PartTransform.of(x + (lx * legDist), y + legY, z + (lz * legDist), 0.25F, legHeight) };
            case LEG_RIGHT -> new PartTransform[]{ PartTransform.of(x - (lx * legDist), y + legY, z - (lz * legDist), 0.25F, legHeight) };

            // PIEDS : Au sol (0.0), hauteur 0.15
            case FOOT_LEFT -> new PartTransform[]{ PartTransform.of(x + (lx * legDist), y + footY, z + (lz * legDist), 0.25F, footHeight) };
            case FOOT_RIGHT -> new PartTransform[]{ PartTransform.of(x - (lx * legDist), y + footY, z - (lz * legDist), 0.25F, footHeight) };
        };
    }
}