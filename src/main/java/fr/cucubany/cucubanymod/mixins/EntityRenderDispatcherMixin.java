package fr.cucubany.cucubanymod.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.cucubany.cucubanymod.hitbox.BodyPartEntity;
import fr.cucubany.cucubanymod.hitbox.IMultiPartPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void onRenderHitbox(PoseStack poseStack, VertexConsumer buffer, Entity entity, float partialTicks, CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;

        // Annule toujours la hitbox blanche par défaut du joueur
        ci.cancel();

        // En mode DEV, les hitboxes sont dessinées dans onRenderLevelLast (centralisé,
        // fonctionne en 1ère et 3ème personne sans duplication)
        if (fr.cucubany.cucubanymod.hitbox.dev.HitboxDevEditor.isActive()) return;

        if (!(player instanceof IMultiPartPlayer multiPartPlayer)) return;
        BodyPartEntity[] parts = multiPartPlayer.getBodyParts();
        if (parts == null) return;

        for (BodyPartEntity part : parts) {
            float r = part.logicalPart.getRed();
            float g = part.logicalPart.getGreen();
            float b = part.logicalPart.getBlue();
            // poseStack est au centre exact du joueur — on ramène la boîte dans ce référentiel
            AABB localBox = part.getBoundingBox().move(-player.getX(), -player.getY(), -player.getZ());
            LevelRenderer.renderLineBox(poseStack, buffer, localBox, r, g, b, 1.0F);
        }
    }
}