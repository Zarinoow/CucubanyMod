package fr.cucubany.cucubanymod.mixins;

import fr.cucubany.cucubanymod.hitbox.BodyPartEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"))
    public EntityHitResult onPick(Entity shooter, Vec3 start, Vec3 end, AABB box, Predicate<Entity> filter, double dist) {
        Predicate<Entity> myFilter = entity -> {
            if (entity instanceof BodyPartEntity part && part.getParent() == shooter) {
                return false;
            }
            return filter.test(entity);
        };
        return ProjectileUtil.getEntityHitResult(shooter, start, end, box, myFilter, dist);
    }
}