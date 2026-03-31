package fr.cucubany.cucubanymod.client.events;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.animation.ClientAnimationManager;
import fr.cucubany.cucubanymod.client.overlay.BankStatementOverlay;
import fr.cucubany.cucubanymod.client.overlay.BodyHealthOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Enregistrement de la factory d'animation
        // Cela ajoute une "couche" transparente sur chaque joueur (et DummyPlayer) pour pouvoir jouer des anims plus tard
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                new ResourceLocation(CucubanyMod.MOD_ID, "animation"),
                42,
                (player) -> {
                    // 1. On crée notre calque
                    ModifierLayer<IAnimation> layer = new ModifierLayer<>();

                    // 2. IMPORTANT : On sauvegarde la référence dans notre Manager !
                    ClientAnimationManager.registerLayer(player, layer);

                    // 3. On retourne le calque pour que PlayerAnimator l'ajoute à la Stack
                    return layer;
                }
        );
        OverlayRegistry.registerOverlayTop("Body Health", BodyHealthOverlay.HUD_OVERLAY);
        OverlayRegistry.registerOverlayTop("Bank Statement", BankStatementOverlay.HUD_OVERLAY);
    }
}
