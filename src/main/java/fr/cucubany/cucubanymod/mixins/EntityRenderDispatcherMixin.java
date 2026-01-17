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
        // On cible uniquement le joueur
        if (entity instanceof Player player) {
            // 1. On annule le rendu de la grosse boîte blanche du joueur
            ci.cancel();

            // 2. Si le joueur possède nos parties (via l'interface), on les dessine NOUS-MÊMES
            if (player instanceof IMultiPartPlayer multiPartPlayer) {
                BodyPartEntity[] parts = multiPartPlayer.getBodyParts();

                if (parts != null) {
                    for (BodyPartEntity part : parts) {
                        // Récupération de la couleur depuis l'Enum
                        float r = part.logicalPart.getRed();
                        float g = part.logicalPart.getGreen();
                        float b = part.logicalPart.getBlue();

                        // CALCUL DE POSITION :
                        // Le 'poseStack' est actuellement centré sur la position interpolée du Joueur.
                        // La 'part.getBoundingBox()' est en coordonnées absolues dans le monde.
                        // Il faut donc soustraire la position du joueur pour ramener la boîte dans le référentiel local.

                        // Note : On utilise les coordonnées exactes du joueur (getX) car le PoseStack a déjà appliqué l'interpolation visuelle.
                        double dx = -player.getX();
                        double dy = -player.getY();
                        double dz = -player.getZ();

                        // On déplace la BoundingBox de la partie pour qu'elle soit relative au joueur
                        AABB localBox = part.getBoundingBox().move(dx, dy, dz);

                        // On dessine la boîte colorée
                        LevelRenderer.renderLineBox(poseStack, buffer, localBox, r, g, b, 1.0F);
                    }
                }
            }
        }
    }
}