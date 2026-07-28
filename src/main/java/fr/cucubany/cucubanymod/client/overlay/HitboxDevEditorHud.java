package fr.cucubany.cucubanymod.client.overlay;

import fr.cucubany.cucubanymod.hitbox.BodyPart;
import fr.cucubany.cucubanymod.hitbox.data.PartTransformData;
import fr.cucubany.cucubanymod.hitbox.data.PoseData;
import fr.cucubany.cucubanymod.hitbox.dev.HitboxDevEditor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.IIngameOverlay;

@OnlyIn(Dist.CLIENT)
public class HitboxDevEditorHud {

    public static final IIngameOverlay HUD_OVERLAY = (gui, poseStack, partialTicks, screenWidth, screenHeight) -> {
        if (!HitboxDevEditor.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        int x = 4;
        int y = 4;
        final int lineH = 10;
        final int titleColor    = 0xFFFF4444;
        final int highlightColor = 0xFFFFFF00;
        final int normalColor   = 0xFFAAAAAA;
        final int hintColor     = 0xFF666666;

        mc.font.drawShadow(poseStack, "[DEV] Hitbox Editor  (H = sortir)", x, y, titleColor);
        y += lineH;

        mc.font.drawShadow(poseStack,
            "Pose: " + HitboxDevEditor.getCurrentPoseName().toUpperCase() + "  (P = changer)",
            x, y, normalColor);
        y += lineH;

        BodyPart part = HitboxDevEditor.getSelectedPart();
        int subIdx = HitboxDevEditor.getSelectedPartIndex();
        mc.font.drawShadow(poseStack,
            "Partie: " + part.getName().toUpperCase() + "[" + subIdx + "]  (Tab / Shift+Tab)",
            x, y, normalColor);
        y += lineH + 2;

        PoseData data = HitboxDevEditor.getEditedPoseData();
        if (data != null) {
            PartTransformData[] arr = data.get(part);
            if (arr != null && subIdx < arr.length) {
                PartTransformData d = arr[subIdx];
                int sel = HitboxDevEditor.getSelectedAxis();
                String[] labels = { "1:lateral ", "2:backward", "3:y       ", "4:width   ", "5:height  " };
                double[] values = { d.lateralOffset, d.backwardOffset, d.yOffset, d.width, d.height };
                for (int i = 0; i < 5; i++) {
                    int color = (i == sel) ? highlightColor : normalColor;
                    String prefix = (i == sel) ? "> " : "  ";
                    mc.font.drawShadow(poseStack,
                        prefix + labels[i] + ": " + String.format("%.4f", values[i]),
                        x, y, color);
                    y += lineH;
                }
            }
        }

        y += 4;
        mc.font.drawShadow(poseStack, "scroll=±0.01  Ctrl+scroll=±0.1", x, y, hintColor);
        y += lineH;
        mc.font.drawShadow(poseStack, "G = exporter JSON", x, y, hintColor);
    };
}
