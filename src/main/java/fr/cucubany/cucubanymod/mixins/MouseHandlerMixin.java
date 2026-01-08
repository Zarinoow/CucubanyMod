package fr.cucubany.cucubanymod.mixins;

import fr.cucubany.cucubanymod.effects.CucubanyEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    /*@Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void injectTripVisionTurn(CallbackInfo ci) {
        if (minecraft.player != null && minecraft.player.hasEffect(CucubanyEffects.TRIP_VISION.get())) {
            MobEffectInstance effect = minecraft.player.getEffect(CucubanyEffects.TRIP_VISION.get());
            int amp = effect != null ? effect.getAmplifier() + 1 : 1;

            // Appliquer une transformation délirante
            double sensitivity = minecraft.options.sensitivity * 0.6 + 0.2;
            double multiplier = Math.pow(sensitivity, 3) * 8.0;

            double dx = accumulatedDX * multiplier;
            double dy = accumulatedDY * multiplier;

            accumulatedDX = 0.0D;
            accumulatedDY = 0.0D;

            // Trip effect: inverser X/Y, amplifier et ajouter un peu de random
            double chaoticDx = -dx * amp + (Math.random() - 0.5) * amp;
            double chaoticDy = -dy * amp + (Math.random() - 0.5) * amp;

            int invertY = minecraft.options.invertYMouse ? -1 : 1;
            minecraft.player.turn(chaoticDx, chaoticDy * invertY);

            ci.cancel(); // bloquer le comportement normal
        }
    }*/

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void debugTest(CallbackInfo ci) {
        System.out.println("[Mixin] turnPlayer called");
    }

}

