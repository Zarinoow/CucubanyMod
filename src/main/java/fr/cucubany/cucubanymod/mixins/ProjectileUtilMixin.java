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

    // --- MÉTHODE 1 : Pour le Regard et le PVP Mêlée ---
    @Redirect(
            method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;")
    )
    private static List<Entity> replacePlayerWithParts_Melee(Level level, Entity shooter, AABB area, Predicate<Entity> filter) {
        return performReplacement(level.getEntities(shooter, area, filter));
    }

    // --- MÉTHODE 2 : Pour les FLÈCHES et PROJECTILES (Celle qu'il manquait) ---
    // Note : Cette méthode prend un float d'inflation à la fin
    @Redirect(
            method = "getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;")
    )
    private static List<Entity> replacePlayerWithParts_Projectile(Level level, Entity projectile, AABB area, Predicate<Entity> filter) {
        // Pour les projectiles, l'entité passée est le projectile lui-même (flèche), pas le tireur.
        return performReplacement(level.getEntities(projectile, area, filter));
    }

    // --- LOGIQUE COMMUNE ---
    private static List<Entity> performReplacement(List<Entity> originalList) {
        List<Entity> newList = new ArrayList<>();

        for (Entity entity : originalList) {
            if (entity instanceof IMultiPartPlayer multiPartPlayer) {
                // On a trouvé un joueur ! On ne l'ajoute PAS à la liste.
                // On ajoute ses parties à la place.
                BodyPartEntity[] parts = multiPartPlayer.getBodyParts();
                if (parts != null) {
                    for (BodyPartEntity part : parts) {
                        // On s'assure que la partie est "active"
                        if (part.isPickable()) {
                            // On ajoute la partie.
                            // Note : On ne vérifie plus le filtre ici de manière stricte car
                            // le filtre de projectile peut être complexe (isAlive, etc).
                            // On laisse ProjectileUtil faire le tri final sur la liste retournée.
                            newList.add(part);
                        }
                    }
                }
            } else {
                // Ce n'est pas un joueur moddé, on le garde.
                newList.add(entity);
            }
        }
        return newList;
    }
}