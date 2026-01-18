package fr.cucubany.cucubanymod.network.skin;

import fr.cucubany.cucubanymod.roleplay.skin.custom.CustomizationData;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CustomizationOptionsPacketHandler {
    public static void handle(CustomizationOptionsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Côté client : stocker les options pour l'UI
            CustomizationData.setOptions(msg.options());
        });
        ctx.get().setPacketHandled(true);
    }
}
