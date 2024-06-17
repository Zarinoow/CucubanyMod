package fr.cucubany.cucubanymod.network;

import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class IdentityChoicePacketHandler {
    public static void handle(IdentityChoicePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                IdentityProvider.setIdentity(player, new Identity(packet.firstName(), packet.lastName()));
                player.refreshDisplayName();
            }
            player.setInvulnerable(false);
        });
        ctx.get().setPacketHandled(true);
    }
}
