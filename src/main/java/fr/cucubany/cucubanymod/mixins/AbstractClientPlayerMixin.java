package fr.cucubany.cucubanymod.mixins;

import fr.cucubany.cucubanymod.client.skin.ClientSkinManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void getSkinTextureLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        // On cast 'this' pour obtenir l'entité joueur
        Player player = (Player) (Object) this;

        // On récupère le skin depuis notre Manager
        ResourceLocation skin = ClientSkinManager.getLocationSkin(player.getUUID());

        // Si un skin custom existe, on l'utilise et on coupe la logique Vanilla
        if (skin != null) {
            cir.setReturnValue(skin);
        }
    }

}