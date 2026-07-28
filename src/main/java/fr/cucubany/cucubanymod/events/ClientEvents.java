package fr.cucubany.cucubanymod.events;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.keybind.KeyBinding;
import fr.cucubany.cucubanymod.client.screen.SkillScreen;
import fr.cucubany.cucubanymod.hitbox.BodyPartEntity;
import fr.cucubany.cucubanymod.hitbox.IMultiPartPlayer;
import fr.cucubany.cucubanymod.hitbox.dev.HitboxDevEditor;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import fr.cucubany.cucubanymod.roleplay.dummy.DummyPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = CucubanyMod.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (KeyBinding.SKILL_SCREEN.consumeClick()) {
            mc.setScreen(new SkillScreen(IdentityProvider.getIdentity(mc.player).getEducation()));
        }

        // Toggle éditeur hitbox (H)
        if (KeyBinding.KEY_HITBOX_DEV.consumeClick()) {
            if (HitboxDevEditor.isAvailable()) {
                if (HitboxDevEditor.isActive()) {
                    HitboxDevEditor.deactivate();
                } else if (mc.player != null) {
                    HitboxDevEditor.activate(mc.player);
                }
            }
        }

        // Contrôles de l'éditeur (uniquement quand actif)
        if (HitboxDevEditor.isActive() && event.getAction() == GLFW.GLFW_PRESS) {
            int key = event.getKey();

            // Tab / Shift+Tab : changer de partie
            if (key == GLFW.GLFW_KEY_TAB) {
                boolean shift = (event.getModifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
                if (shift) HitboxDevEditor.cyclePartBackward();
                else       HitboxDevEditor.cyclePartForward();
            }

            // 1-5 : sélectionner l'axe
            if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_5) {
                HitboxDevEditor.selectAxis(key - GLFW.GLFW_KEY_1);
            }

            // P : basculer la pose (standing ↔ sneak)
            if (key == GLFW.GLFW_KEY_P) {
                HitboxDevEditor.togglePose();
            }

            // G : exporter le JSON
            if (key == GLFW.GLFW_KEY_G) {
                HitboxDevEditor.exportToFile();
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollEvent event) {
        if (!HitboxDevEditor.isActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        long window = mc.getWindow().getWindow();
        boolean coarse = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                      || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        HitboxDevEditor.scrollAdjust(event.getScrollDelta(), coarse);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelLastEvent event) {
        if (!HitboxDevEditor.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer realPlayer = mc.player;
        if (realPlayer == null) return;

        DummyPlayer ghost = HitboxDevEditor.getGhostEntity();
        if (ghost == null) return;

        Vec3 ghostPos = HitboxDevEditor.getGhostPos();
        float pt = event.getPartialTick();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // L'entité fantôme n'est jamais tickée → son état (vanilla, AnimLib, autres mods)
        // reste figé au snapshot pris lors de l'activation.
        mc.getEntityRenderDispatcher().render(
            ghost,
            ghostPos.x - camPos.x,
            ghostPos.y - camPos.y,
            ghostPos.z - camPos.z,
            HitboxDevEditor.getGhostYBodyRot(),
            pt,
            event.getPoseStack(),
            bufferSource,
            mc.getEntityRenderDispatcher().getPackedLightCoords(ghost, pt)
        );

        // Hitboxes dessinées centralisées ici (1ère et 3ème personne, sans duplication)
        if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()
                && realPlayer instanceof IMultiPartPlayer multiPartPlayer) {
            BodyPartEntity[] parts = multiPartPlayer.getBodyParts();
            if (parts != null) {
                event.getPoseStack().pushPose();
                event.getPoseStack().translate(ghostPos.x - camPos.x, ghostPos.y - camPos.y, ghostPos.z - camPos.z);
                VertexConsumer lineBuffer = bufferSource.getBuffer(RenderType.lines());
                for (BodyPartEntity part : parts) {
                    float r = part.logicalPart.getRed();
                    float g2 = part.logicalPart.getGreen();
                    float b2 = part.logicalPart.getBlue();
                    AABB localBox = part.getBoundingBox().move(-ghostPos.x, -ghostPos.y, -ghostPos.z);
                    LevelRenderer.renderLineBox(event.getPoseStack(), lineBuffer, localBox, r, g2, b2, 1.0f);
                }
                event.getPoseStack().popPose();
            }
        }

        bufferSource.endBatch();
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameplateEvent event) {
        if (event.getEntity() instanceof DummyPlayer) {
            event.setResult(Event.Result.DENY);
        }
    }
}
