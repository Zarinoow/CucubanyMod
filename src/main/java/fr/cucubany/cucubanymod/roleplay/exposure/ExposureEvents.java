package fr.cucubany.cucubanymod.roleplay.exposure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.hallucination.HallucinationManager;
import fr.cucubany.cucubanymod.sounds.CucubanySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ExposureEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;

        if (player.level.dimension().location().toString().equals("cucubany:exterieur")) {
            ExposureHandler.incrementExposure(player);

            HallucinationManager.tick(Minecraft.getInstance());
        } else {
            ExposureHandler.resetExposure(player);
            HallucinationManager.clear();
        }
    }

    private static final ResourceLocation VIGNETTE = new ResourceLocation(CucubanyMod.MOD_ID, "textures/misc/vignette.png");
    private static final ResourceLocation BRAIN_OVERLAY = new ResourceLocation(CucubanyMod.MOD_ID, "textures/gui/brain_overlay.png");

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.player == null) return;

        Player player = mc.player;
        int exposure = ExposureHandler.getExposure(player);
        int level = ExposureHandler.getExposureLevel(player);

        // ---------- BRAIN ICON ----------
        if (player.level.dimension().location().toString().equals("cucubany:exterieur")) {
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int x = screenWidth - 74;
            int y = 10;
            int textureY = level * 64;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, BRAIN_OVERLAY);
            RenderSystem.enableBlend();
            GuiComponent.blit(event.getMatrixStack(), x, y, 0, textureY, 64, 64, 64, 576);
            RenderSystem.disableBlend();
        }

        // ---------- VIGNETTE EFFECT ----------
        if (level >= 5) {
            float alpha;
            if (level == 5) {
                alpha = (exposure / (float) ExposureHandler.MAX_EXPOSURE) * 0.3f; // 0.0 → 0.3
            } else {
                alpha = 0.3f; // Niveau 6+
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 0.2F, 0.2F, alpha);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VIGNETTE);

            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buffer = tess.getBuilder();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(0, event.getWindow().getGuiScaledHeight(), 0).uv(0, 1).endVertex();
            buffer.vertex(event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), 0).uv(1, 1).endVertex();
            buffer.vertex(event.getWindow().getGuiScaledWidth(), 0, 0).uv(1, 0).endVertex();
            buffer.vertex(0, 0, 0).uv(0, 0).endVertex();
            tess.end();

            RenderSystem.disableBlend();
        }

        // ---------- EFFET SUPPLÉMENTAIRE ----------
        if (level >= 6 && !player.hasEffect(MobEffects.CONFUSION)) {
            player.playSound(CucubanySounds.STRESS_LOOP.get(), 0.6F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return; // test avec AFTER_SOLID_BLOCKS
        HallucinationManager.render(event);
    }

}
