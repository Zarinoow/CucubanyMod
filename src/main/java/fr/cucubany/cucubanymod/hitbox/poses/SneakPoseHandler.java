package fr.cucubany.cucubanymod.hitbox.poses;

import fr.cucubany.cucubanymod.hitbox.BodyPart;
import fr.cucubany.cucubanymod.hitbox.PartTransform;
import fr.cucubany.cucubanymod.hitbox.data.PosesRegistry;
import net.minecraft.world.entity.player.Player;

public class SneakPoseHandler implements IPartPoseHandler {

    @Override
    public PartTransform[] getTransforms(Player player, BodyPart part) {
        return PosesRegistry.get("sneak")
            .getTransforms(player.getX(), player.getY(), player.getZ(), player.yBodyRot, part);
    }
}