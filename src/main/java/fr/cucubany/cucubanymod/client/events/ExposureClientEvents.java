package fr.cucubany.cucubanymod.client.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.hallucination.HallucinationManager;
import fr.cucubany.cucubanymod.sounds.CucubanySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// L'annotation ci-dessous est CRUCIALE : Elle dit "Ne charge ça que sur le CLIENT"
@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID, value = Dist.CLIENT)
public class ExposureClientEvents {

    private static final ResourceLocation VIGNETTE = new ResourceLocation(CucubanyMod.MOD_ID, "textures/misc/vignette.png");
    private static final ResourceLocation BRAIN_OVERLAY = new ResourceLocation(CucubanyMod.MOD_ID, "textures/gui/brain_overlay.png");

    // On remet un PlayerTick ici MAIS uniquement pour la partie visuelle (Hallucinations)
    @SubscribeEvent
    public static void onClientPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        // Vérification importante : s'assurer qu'on est bien sur le joueur du client
        if (event.player != Minecraft.getInstance().player) return;

        if (event.player.level.dimension().location().toString().equals("cucubany:exterieur")) {
            // Note : on passe Minecraft.getInstance() ici sans peur car on est dans une classe CLIENT
            HallucinationManager.tick(Minecraft.getInstance());
        } else {
            HallucinationManager.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        // ... Ton code de rendu VIGNETTE et BRAIN ICON ...
        // (Copie-colle tout le contenu de ton ancienne méthode onRenderOverlay ici)
        // ...
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return;
        HallucinationManager.render(event);
    }
}
