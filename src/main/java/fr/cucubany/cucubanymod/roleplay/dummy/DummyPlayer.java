package fr.cucubany.cucubanymod.roleplay.dummy;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;

public class DummyPlayer extends RemotePlayer {

    private ResourceLocation currentSkin = new ResourceLocation("minecraft", "textures/entity/steve.png");
    private String modelType = "default";

    public DummyPlayer(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    public void setSkin(ResourceLocation skin) {
        this.currentSkin = skin;
    }

    public void setModelType(boolean isSlim) {
        this.modelType = isSlim ? "slim" : "default";
    }

    @Override
    public String getModelName() {
        return modelType;
    }

    @Override
    public ResourceLocation getSkinTextureLocation() {
        return currentSkin;
    }
}
