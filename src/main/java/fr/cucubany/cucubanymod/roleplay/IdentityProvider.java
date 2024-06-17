package fr.cucubany.cucubanymod.roleplay;

import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

public class IdentityProvider {
    @Nullable
    public static Identity getIdentity(Player player) {
        return getIdentity(player.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY));
    }

    @Nullable
    public static Identity getIdentity(ServerPlayer player) {
        return getIdentity(player.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY));
    }

    @Nullable
    private static Identity getIdentity(LazyOptional<IIdentityCapability> capability) {
        Identity identity = null;
        if (capability.isPresent()) {
            identity = capability.orElseThrow(() -> new IllegalStateException("Could not get identity")).getIdentity();
        }
        return identity;
    }

    public static void setIdentity(Player player, Identity identity) {
        LazyOptional<IIdentityCapability> capability = player.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);
        capability.ifPresent(cap -> cap.setIdentity(identity));
    }

    public static void setIdentity(ServerPlayer player, Identity identity) {
        LazyOptional<IIdentityCapability> capability = player.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);
        capability.ifPresent(cap -> cap.setIdentity(identity));
    }
}
