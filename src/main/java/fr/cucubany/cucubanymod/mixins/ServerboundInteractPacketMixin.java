package fr.cucubany.cucubanymod.mixins;

import fr.cucubany.cucubanymod.server.PartRegistry;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerboundInteractPacket.class)
public class ServerboundInteractPacketMixin {

    @Shadow @Final private int entityId;

    /**
     * Quand le serveur demande "Quelle est l'entité cible de ce paquet ?",
     * si le monde ne la trouve pas, on la donne depuis notre registre.
     */
    @Inject(method = "getTarget", at = @At("RETURN"), cancellable = true)
    private void onGetTarget(ServerLevel level, CallbackInfoReturnable<Entity> cir) {
        // Si le jeu n'a rien trouvé (cir.getReturnValue() == null)
        if (cir.getReturnValue() == null) {
            // On cherche dans nos parties
            Entity part = PartRegistry.get(this.entityId);
            if (part != null) {
                // On retourne la partie !
                cir.setReturnValue(part);
            }
        }
    }
}