package fr.cucubany.cucubanymod.hitbox.poses;

import fr.cucubany.cucubanymod.hitbox.BodyPart;
import fr.cucubany.cucubanymod.hitbox.PartTransform;
import net.minecraft.world.entity.player.Player;

public interface IPartPoseHandler {
    /**
     * Retourne un tableau de transforms (Position + Taille) pour chaque sous-entité de la partie.
     */
    PartTransform[] getTransforms(Player player, BodyPart part);
}