package fr.cucubany.cucubanymod.client.hallucination;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.roleplay.exposure.ExposureHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HallucinationManager {

    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation(CucubanyMod.MOD_ID, "textures/entity/shadow.png")
    };

    private static class HallucinationData {
        BlockPos pos;
        int textureIndex;

        public HallucinationData(BlockPos pos, int textureIndex) {
            this.pos = pos;
            this.textureIndex = textureIndex;
        }
    }

    private static final List<HallucinationData> activeHallucinations = new ArrayList<>();

    // Configuration
    private static final int START_LEVEL = 3; // Niveau où les ombres commencent à apparaitre
    private static final int MIN_DISTANCE = 15; // Distance min (pour ne pas spawn SUR le joueur)
    private static final int MAX_DISTANCE = 40; // Rayon de spawn
    private static final int DESPAWN_DISTANCE = 50; // Distance à laquelle l'hallucination disparait
    private static final int MAX_TRIES = 30;

    public static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;

        int currentLevel = ExposureHandler.getExposureLevel(mc.player);

        if (currentLevel < START_LEVEL) {
            if (!activeHallucinations.isEmpty()) {
                activeHallucinations.clear();
            }
            return;
        }

        int maxHallucinations = (currentLevel - START_LEVEL) + 1;

        // Nettoyage des hallucinations trop loin
        // On vérifie la distance en utilisant h.pos
        activeHallucinations.removeIf(h ->
                Math.sqrt(h.pos.distToCenterSqr(mc.player.position())) > DESPAWN_DISTANCE);

        if (activeHallucinations.size() < maxHallucinations) {
            if (mc.level.random.nextInt(15) != 0) return;

            BlockPos origin = mc.player.blockPosition();
            Random rand = mc.level.random;

            for (int i = 0; i < MAX_TRIES; i++) {
                double angle = rand.nextDouble() * 2 * Math.PI;
                double distance = MIN_DISTANCE + rand.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
                int dx = (int) (Math.cos(angle) * distance);
                int dz = (int) (Math.sin(angle) * distance);
                int dy = rand.nextInt(10) - 5;

                BlockPos candidate = origin.offset(dx, dy, dz);

                if (!mc.level.hasChunkAt(candidate)) continue;

                if (mc.level.isEmptyBlock(candidate) &&
                        mc.level.isEmptyBlock(candidate.above()) &&
                        !mc.level.isEmptyBlock(candidate.below())) {

                    // --- 3. Choix d'une texture aléatoire au spawn ---
                    int randomTexIndex = rand.nextInt(TEXTURES.length);
                    activeHallucinations.add(new HallucinationData(candidate, randomTexIndex));
                    break;
                }
            }
        }
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return;
        if (activeHallucinations.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();
        Vec3 playerPos = mc.player.position();

        RenderSystem.enableTexture();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.getBuilder();

        for (HallucinationData data : activeHallucinations) {
            BlockPos pos = data.pos;

            RenderSystem.setShaderTexture(0, TEXTURES[data.textureIndex]);

            poseStack.pushPose();

            double x = pos.getX() - camPos.x + 0.5;
            double y = pos.getY() - camPos.y;
            double z = pos.getZ() - camPos.z + 0.5;

            poseStack.translate(x, y, z);

            double dx = playerPos.x - (pos.getX() + 0.5);
            double dz = playerPos.z - (pos.getZ() + 0.5);
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx))) - 90f;
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-yaw));

            Matrix4f matrix = poseStack.last().pose();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            float width = 1.0f;
            float height = 2.0f;

            buffer.vertex(matrix, -width, 0, 0).uv(0.0F, 1.0F).endVertex();
            buffer.vertex(matrix, width, 0, 0).uv(1.0F, 1.0F).endVertex();
            buffer.vertex(matrix, width, height, 0).uv(1.0F, 0.0F).endVertex();
            buffer.vertex(matrix, -width, height, 0).uv(0.0F, 0.0F).endVertex();

            tess.end();
            poseStack.popPose();
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // Utile pour vider la liste si le joueur change de dimension ou se déco
    public static void clear() {
        activeHallucinations.clear();
    }
}