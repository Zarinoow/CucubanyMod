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

        // Hauteurs Standing
        double headY = 1.375;
        double torsoY = 0.75;
        double armY = 0.65625;
        double legY = 0.0;

        // Distances
        double torsoDist = 0.125;
        double legDist = 0.125;
        double armDist = 0.375;

        return switch (part) {
            case HEAD -> new PartTransform[]{
                    PartTransform.of(x, y + headY, z, 0.5F, 0.5F)
            };

            case TORSO -> new PartTransform[]{
                    // Partie Gauche (Index 0) - Décalée à gauche
                    PartTransform.of(x + (lx * torsoDist), y + torsoY, z + (lz * torsoDist), 0.25F, 0.65F),
                    // Partie Droite (Index 1) - Décalée à droite
                    PartTransform.of(x - (lx * torsoDist), y + torsoY, z - (lz * torsoDist), 0.25F, 0.65F)
            };

            case ARM_LEFT -> new PartTransform[]{ PartTransform.of(x + (lx * armDist), y + armY, z + (lz * armDist), 0.25F, 0.75F) };
            case ARM_RIGHT -> new PartTransform[]{ PartTransform.of(x - (lx * armDist), y + armY, z - (lz * armDist), 0.25F, 0.75F) };
            case LEG_LEFT -> new PartTransform[]{ PartTransform.of(x + (lx * legDist), y + legY, z + (lz * legDist), 0.25F, 0.75F) };
            case LEG_RIGHT -> new PartTransform[]{ PartTransform.of(x - (lx * legDist), y + legY, z - (lz * legDist), 0.25F, 0.75F) };
        };
    }
}