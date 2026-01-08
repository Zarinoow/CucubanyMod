package fr.cucubany.cucubanymod.events;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.effects.CucubanyEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID, value = Dist.CLIENT)
public class TrippyEffectHandler {

    private static boolean invertControls = false;
    private static int tickCounter = 0;
    private static int nextFlipTick = 0;
    private static Random random = new Random();

    @SubscribeEvent
    public static void onInputUpdate(MovementInputUpdateEvent event) {
        LocalPlayer player = (LocalPlayer) event.getPlayer();

        if (player.hasEffect(CucubanyEffects.TRIP_VISION.get())) {
            MobEffectInstance effect = player.getEffect(CucubanyEffects.TRIP_VISION.get());
            int amp = effect != null ? effect.getAmplifier() + 1 : 1;

            tickCounter++;
            if (tickCounter >= nextFlipTick) {
                invertControls = !invertControls;
                // Nouvelle durée aléatoire avant la prochaine inversion
                nextFlipTick = tickCounter + 20 + random.nextInt(60 / amp); // entre 1s et ~4s
            }


            Input input = event.getInput();

            if (invertControls) {
                // Inverser les contrôles de mouvement avec un petit facteur aléatoire
                if (input.forwardImpulse != 0) {
                    input.forwardImpulse *= -1.0F;
                    input.forwardImpulse += (random.nextFloat() - 0.5F) * 0.2F; // petit facteur aléatoire
                }
                if (input.leftImpulse != 0) {
                    input.leftImpulse *= -1.0F;
                    input.leftImpulse += (random.nextFloat() - 0.5F) * 0.2F;
                }
            }
        } else {
            tickCounter = 0;
            invertControls = false;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        LocalPlayer player = mc.player;

        if (player.hasEffect(CucubanyEffects.TRIP_VISION.get())) {
            MobEffectInstance effect = player.getEffect(CucubanyEffects.TRIP_VISION.get());
            int amp = effect != null ? effect.getAmplifier() + 1 : 1;

            // Appliquer un léger random à l'intensité de la caméra
            float camIntensity = (0.3F * amp) + (random.nextFloat() * 0.2f); // Aléatoire sur l'intensité

            // Rotation aléatoire de la caméra
            player.setYRot(player.getYRot() + (float)(Math.sin(tickCounter * 0.1 + random.nextFloat()) * camIntensity));
            player.setXRot(player.getXRot() + (float)(Math.cos(tickCounter * 0.13 + random.nextFloat()) * camIntensity * 0.5));
        }
    }

}
