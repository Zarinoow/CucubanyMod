package fr.cucubany.cucubanymod.mixins;

import com.mojang.authlib.GameProfile;
import fr.cucubany.cucubanymod.client.skin.ClientSkinManager;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {

    @Shadow @Final private GameProfile profile;

    // Gère le modèle (Slim / Default) pour les bras
    @Inject(method = "getModelName", at = @At("HEAD"), cancellable = true)
    private void getModelName(CallbackInfoReturnable<String> cir) {
        if (ClientSkinManager.isPlayerSlim(this.profile.getId())) {
            cir.setReturnValue("slim");
        } else if (ClientSkinManager.hasCustomSkin(this.profile.getId())) {
            cir.setReturnValue("default");
        }
    }

    // Gère la texture dans la Tablist
    @Inject(method = "getSkinLocation", at = @At("HEAD"), cancellable = true)
    private void getSkinLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation skin = ClientSkinManager.getLocationSkin(this.profile.getId());
        if (skin != null) {
            cir.setReturnValue(skin);
        }
    }
}