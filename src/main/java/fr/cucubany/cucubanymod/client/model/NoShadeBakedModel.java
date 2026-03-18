package fr.cucubany.cucubanymod.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Désactive uniquement l'Ambient Occlusion sur le modèle.
 * Le contrôle de la lumière par voisin est géré par FixedLightConsumer dans le BER.
 */
public class NoShadeBakedModel implements BakedModel {

    private final BakedModel wrapped;

    public NoShadeBakedModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand) {
        return wrapped.getQuads(state, side, rand);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        return wrapped.getQuads(state, side, rand, extraData);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() { return wrapped.isGui3d(); }

    @Override
    public boolean usesBlockLight() { return wrapped.usesBlockLight(); }

    @Override
    public boolean isCustomRenderer() { return wrapped.isCustomRenderer(); }

    @Override
    public TextureAtlasSprite getParticleIcon() { return wrapped.getParticleIcon(); }

    @Override
    public ItemTransforms getTransforms() { return wrapped.getTransforms(); }

    @Override
    public ItemOverrides getOverrides() { return wrapped.getOverrides(); }
}