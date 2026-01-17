package fr.cucubany.cucubanymod.hitbox;

import fr.cucubany.cucubanymod.hitbox.poses.IPartPoseHandler;
import fr.cucubany.cucubanymod.hitbox.poses.SneakPoseHandler;
import fr.cucubany.cucubanymod.hitbox.poses.StandingPoseHandler;
import net.minecraft.world.entity.player.Player;

public class PoseManager {
    private static final IPartPoseHandler STANDING = new StandingPoseHandler();
    private static final IPartPoseHandler SNEAK = new SneakPoseHandler();
    // Add Swimming/Crawling here later

    public static IPartPoseHandler getHandler(Player player) {
        // Prioritize specific states
        if (player.isVisuallySwimming()) {
            return SNEAK; // Use swimming handler later
        }
        if (player.isCrouching()) {
            return SNEAK;
        }
        return STANDING;
    }
}