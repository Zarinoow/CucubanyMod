package fr.cucubany.cucubanymod.network;

import fr.cucubany.cucubanymod.client.screen.IdentityGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenIdentityScreenPacketHandler {
    public static void handle(OpenIdentityScreenPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> OpenIdentityScreenPacketHandler::openScreen);
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen() {
        Minecraft.getInstance().setScreen(new IdentityGui());
    }
}

