package fr.cucubany.cucubanymod.roleplay.dummy;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;

public class DummyPlayer extends RemotePlayer {

    private ResourceLocation currentSkin = new ResourceLocation("minecraft", "textures/entity/steve.png");
    private String modelType = "default";

    /** Bitmask des couches overlay visibles — 127 = toutes visibles par défaut. */
    private byte visibleLayerMask = (byte) 127;

    public DummyPlayer(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    public void setSkin(ResourceLocation skin) {
        this.currentSkin = skin;
    }

    public void setModelType(boolean isSlim) {
        this.modelType = isSlim ? "slim" : "default";
    }

    /**
     * Configure quelles couches overlay (hat, jacket, sleeves, pants) sont visibles.
     * Utilisé par drawPart3D pour n'afficher que les couches pertinentes par partie du corps.
     * @param mask bitmask PlayerModelPart — 127 = tout visible
     */
    public void setVisibleLayerMask(byte mask) {
        this.visibleLayerMask = mask;
    }

    @Override
    public String getModelName() {
        return modelType;
    }

    @Override
    public ResourceLocation getSkinTextureLocation() {
        return currentSkin;
    }

    /**
     * Override pour contrôler la visibilité des couches overlay via {@link #visibleLayerMask}.
     * Sans cet override, DATA_PLAYER_MODE_CUSTOMISATION vaut 0 par défaut pour les DummyPlayer
     * → toutes les couches overlay (hat, jacket…) seraient cachées par PlayerModel.setupAnim().
     */
    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return (visibleLayerMask & part.getMask()) != 0;
    }
}