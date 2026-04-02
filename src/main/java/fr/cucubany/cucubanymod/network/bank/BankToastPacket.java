package fr.cucubany.cucubanymod.network.bank;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Paquet Serveur → Client : notification toast d'un virement reçu. */
public record BankToastPacket(String amount, String senderName) {

    public static void encode(BankToastPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.amount);
        buf.writeUtf(p.senderName);
    }

    public static BankToastPacket decode(FriendlyByteBuf buf) {
        return new BankToastPacket(buf.readUtf(), buf.readUtf());
    }

    public static void handle(BankToastPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> showToast(packet))
        );
        ctx.get().setPacketHandled(true);
    }

    private static void showToast(BankToastPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        SystemToast.addOrUpdate(
            mc.getToasts(),
            SystemToast.SystemToastIds.TUTORIAL_HINT,
            new TextComponent(I18n.get("bank.cucubanymod.toast.received")),
            new TextComponent(packet.amount() + " " + I18n.get("bank.cucubanymod.toast.from") + " " + packet.senderName())
        );
    }
}
