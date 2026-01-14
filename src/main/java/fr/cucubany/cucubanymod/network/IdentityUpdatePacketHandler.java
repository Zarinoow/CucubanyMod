package fr.cucubany.cucubanymod.network;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.skin.WildfireBridge; // Import du Bridge
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        if (Minecraft.getInstance().level == null) return;

        Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.uuid());
        if (player == null) {
            return;
        }

        IdentityProvider.setIdentity(player, packet.identity());

        if (player == Minecraft.getInstance().player && !packet.identity().getFirstName().isEmpty()) {
            WildfireBridge.setLock(player, true);
        }
    }
}