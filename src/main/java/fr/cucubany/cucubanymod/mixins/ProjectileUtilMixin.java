package fr.cucubany.cucubanymod.mixins;

import fr.cucubany.cucubanymod.hitbox.BodyPartEntity;
import fr.cucubany.cucubanymod.hitbox.IMultiPartPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {

    /**
     * Intercepte la récupération des entités lors d'un calcul de collision (Flèche, Coup d'épée, Regard).
     * On ajoute manuellement les BodyPartEntity à la liste des candidats potentiels.
     */
    @Redirect(
            method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;")
    )
    private static List<Entity> replacePlayerWithParts(Level level, Entity shooter, AABB area, Predicate<Entity> filter) {
        List<Entity> originalList = level.getEntities(shooter, area, filter);
        List<Entity> newList = new ArrayList<>();

        for (Entity entity : originalList) {
            if (entity instanceof IMultiPartPlayer multiPartPlayer) {
                BodyPartEntity[] parts = multiPartPlayer.getBodyParts();
                if (parts != null) {
                    for (BodyPartEntity part : parts) {
                        if (part.isPickable()) {
                            if (filter == null || filter.test(part)) {
                                newList.add(part);
                            }
                        }
                    }
                }
            } else {
                newList.add(entity);
            }
        }
        return newList;
    }
}