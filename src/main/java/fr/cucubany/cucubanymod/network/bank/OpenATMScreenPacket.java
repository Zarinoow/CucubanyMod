package fr.cucubany.cucubanymod.network.bank;

import fr.cucubany.cucubanymod.client.screen.ATMScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Paquet Serveur → Client : ouvre l'écran ATM. */
public record OpenATMScreenPacket(BlockPos pos) {

    public static void encode(OpenATMScreenPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
    }

    public static OpenATMScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenATMScreenPacket(buf.readBlockPos());
    }

    public static void handle(OpenATMScreenPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                Minecraft.getInstance().setScreen(new ATMScreen(packet.pos()))
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
