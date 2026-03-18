package fr.cucubany.cucubanymod.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.cucubany.cucubanymod.blocks.clothing.BigClosetBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import fr.cucubany.cucubanymod.blocks.clothing.ClassicClosetBlock;
import fr.cucubany.cucubanymod.blocks.clothing.ClosetBlock;
import fr.cucubany.cucubanymod.blocks.clothing.ClosetBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

public class ClosetBlockEntityRenderer implements BlockEntityRenderer<ClosetBlockEntity> {

    private static final RenderType CLOSET_CUTOUT = new RenderStateShard("closet_build", () -> {}, () -> {}) {
        RenderType make() {
            return RenderType.create(
                    "cucubanymod:closet_cutout",
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

    public ClosetBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ClosetBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {

        BlockState state = be.getBlockState();

        // ADAPTATION MULTI-TAILLES :
        // 1. Si c'est un bloc à 2 étages (possède la propriété HALF), on ne rend que la partie LOWER.
        if (state.hasProperty(ClassicClosetBlock.HALF)) {
            if (state.getValue(ClassicClosetBlock.HALF) != DoubleBlockHalf.LOWER) return;
        }

        // 2. Pour les futurs blocs 4x4, si tu utilises une propriété genre "MASTER_BLOCK" ou une position:
        // if (state.hasProperty(TonBlocGeant.IS_MASTER) && !state.getValue(TonBlocGeant.IS_MASTER)) return;

        Level level = be.getLevel();
        if (level == null) return;

        // Pour le BigCloset, décaler le modèle d'1 bloc vers sa gauche (direction du bloc OTHER).
        // La gauche du modèle vue de face = facing.getClockWise().
        boolean isBigCloset = state.getBlock() instanceof BigClosetBlock;
        if (isBigCloset) {
            Direction left = state.getValue(ClosetBlock.FACING).getClockWise();
            poseStack.pushPose();
            poseStack.translate(left.getStepX(), 0, left.getStepZ());
        }

        BlockPos renderPos = isBigCloset
                ? be.getBlockPos().relative(state.getValue(ClosetBlock.FACING).getClockWise())
                : be.getBlockPos();

        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

        ForgeHooksClient.setRenderType(CLOSET_CUTOUT);
        VertexConsumer consumer = new FixedLightConsumer(buffers.getBuffer(CLOSET_CUTOUT), packedLight);

        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        renderer.tesselateWithoutAO(
                level, model, state, renderPos,
                poseStack, consumer, false, new Random(), 42L, packedOverlay,
                EmptyModelData.INSTANCE
        );

        ForgeHooksClient.setRenderType(null);

        if (isBigCloset) {
            poseStack.popPose();
        }

        if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(CLOSET_CUTOUT);
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