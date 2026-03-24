package fr.cucubany.cucubanymod.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.blocks.IBBBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

public class BlockBenchEntityRenderer<T extends BlockEntity & IBBBlockEntity> implements BlockEntityRenderer<T> {

    private static final RenderType BB_CUTOUT = new RenderStateShard("bb_build", () -> {}, () -> {}) {
        RenderType make() {
            return RenderType.create(
                    CucubanyMod.MOD_ID + ":bb_cutout",
                    DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.QUADS,
                    131072, true, false,
                    RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_CUTOUT_SHADER)
                            .setTextureState(new TextureStateShard(
                                    TextureAtlas.LOCATION_BLOCKS, false, false))
                            .setTransparencyState(NO_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .createCompositeState(true));
        }
    }.make();

    public BlockBenchEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(T be, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {

        // 1. On vérifie si c'est la partie principale (évite les doublons de rendu)
        if (!be.isMainPart()) return;

        Level level = be.getLevel();
        BlockState state = be.getBlockState();
        if (level == null) return;

        // 2. Récupération du décalage via l'interface
        Vec3 offset = be.getModelOffset();

        poseStack.pushPose();
        if (offset != Vec3.ZERO) {
            poseStack.translate(offset.x, offset.y, offset.z);
        }

        // Le modèle est rendu à sa position réelle + l'offset visuel
        BlockPos renderPos = be.getBlockPos().offset(offset.x, offset.y, offset.z);
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

        ForgeHooksClient.setRenderType(BB_CUTOUT);
        VertexConsumer consumer = new FixedLightConsumer(buffers.getBuffer(BB_CUTOUT), packedLight);

        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        renderer.tesselateWithoutAO(
                level, model, state, renderPos,
                poseStack, consumer, false, new Random(), 42L, packedOverlay,
                EmptyModelData.INSTANCE
        );

        ForgeHooksClient.setRenderType(null);
        poseStack.popPose();

        if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(BB_CUTOUT);
        }
    }

    private static class FixedLightConsumer implements VertexConsumer {

        private final VertexConsumer delegate;
        private final int lightU;
        private final int lightV;

        FixedLightConsumer(VertexConsumer delegate, int packedLight) {
            this.delegate = delegate;
            this.lightU = packedLight & 0xFFFF;
            this.lightV = (packedLight >> 16) & 0xFFFF;
        }

        @Override public VertexConsumer vertex(double x, double y, double z) { delegate.vertex(x, y, z); return this; }
        @Override public VertexConsumer color(int r, int g, int b, int a) { delegate.color(r, g, b, a); return this; }
        @Override public VertexConsumer uv(float u, float v) { delegate.uv(u, v); return this; }
        @Override public VertexConsumer overlayCoords(int u, int v) { delegate.overlayCoords(u, v); return this; }
        @Override public VertexConsumer uv2(int u, int v) { delegate.uv2(lightU, lightV); return this; }
        @Override public VertexConsumer normal(float x, float y, float z) { delegate.normal(x, y, z); return this; }
        @Override public void endVertex() { delegate.endVertex(); }
        @Override public void defaultColor(int r, int g, int b, int a) { delegate.defaultColor(r, g, b, a); }
        @Override public void unsetDefaultColor() { delegate.unsetDefaultColor(); }
    }
}