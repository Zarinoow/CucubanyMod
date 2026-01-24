package fr.cucubany.cucubanymod.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.capabilities.BodyHealthProvider;
import fr.cucubany.cucubanymod.config.CucubanyClientConfigs;
import fr.cucubany.cucubanymod.hitbox.BodyPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.IIngameOverlay;

public class BodyHealthOverlay {

    private static final ResourceLocation HEALTH_TEXTURE = new ResourceLocation(CucubanyMod.MOD_ID, "textures/gui/health_overlay.png");
    private static final int SCREEN_X_OFFSET = 5;
    private static final int SCREEN_Y_OFFSET = 5;
    private static final int SKIN_RENDER_OFFSET_X = 1;
    private static final int SKIN_RENDER_OFFSET_Y = 1;

    public static final IIngameOverlay HUD_OVERLAY = (gui, poseStack, partialTicks, width, height) -> {
        Minecraft mc = Minecraft.getInstance();

        LocalPlayer player = mc.player;

        if (player.isSpectator() || mc.options.hideGui) return;

        ResourceLocation playerSkinLocation = player.getSkinTextureLocation();
        boolean isSlim = player.getModelName().equals("slim");

        player.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(health -> {

            RenderSystem.enableBlend();
            poseStack.pushPose();
            float scaleFactor = CucubanyClientConfigs.HEALTH_OVERLAY_SCALE.get().floatValue();
            poseStack.scale(scaleFactor, scaleFactor, 1.0F);

            int baseX = SCREEN_X_OFFSET;
            int baseY = SCREEN_Y_OFFSET;

            // --- COUCHE 1 : SKIN ---
            RenderSystem.setShaderTexture(0, playerSkinLocation);

            for (BodyPart part : BodyPart.values()) {
                int drawX = baseX + part.getGuiX() + SKIN_RENDER_OFFSET_X;
                int drawY = baseY + part.getGuiY() + SKIN_RENDER_OFFSET_Y;

                int skinU = part.getSkinU();

                if(!isSlim && (part == BodyPart.ARM_LEFT || part == BodyPart.ARM_RIGHT)) {
                    skinU++;
                }

                GuiComponent.blit(poseStack, drawX, drawY,
                        skinU, part.getSkinV(),
                        part.getSkinWidth(), part.getSkinHeight(),
                        64, 64);
            }

            // --- COUCHE 2 : SANTÉ ---
            RenderSystem.setShaderTexture(0, HEALTH_TEXTURE);

            for (BodyPart part : BodyPart.values()) {
                float currentHp = health.getHealth(part);
                float maxHp = health.getMaxHealth(part);
                float percent = currentHp / maxHp;

                int gridIndex = (int) ((1.0f - percent) * 8.0f);
                gridIndex = Math.max(0, Math.min(8, gridIndex));

                int col = gridIndex % 3;
                int row = gridIndex / 3;
                int cellU = col * 40;
                int cellV = row * 40;

                int partU = cellU + part.getGuiX();
                int partV = cellV + part.getGuiY();

                int drawX = baseX + part.getGuiX();
                int drawY = baseY + part.getGuiY();

                GuiComponent.blit(poseStack, drawX, drawY,
                        partU, partV,
                        part.getGuiWidth(), part.getGuiHeight(),
                        128, 128);
            }

            poseStack.popPose();
            RenderSystem.disableBlend();
        });
    };
}