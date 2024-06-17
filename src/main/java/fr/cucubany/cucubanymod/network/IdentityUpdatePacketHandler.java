package fr.cucubany.cucubanymod.network;

import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class IdentityUpdatePacketHandler {

    public static void handle(IdentityUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> updateIdentity(packet));
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void updateIdentity(IdentityUpdatePacket packet) {
        Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.uuid());

        LazyOptional<IIdentityCapability> identityCap = player.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);
        identityCap.ifPresent(cap -> {
            cap.setIdentity(packet.identity());
        });
    }

}
