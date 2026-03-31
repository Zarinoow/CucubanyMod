package fr.cucubany.cucubanymod.network.bank;

import fr.cucubany.cucubanymod.sounds.CucubanySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Paquet C→S : demande au serveur de jouer le bip ATM à la position du block (entendu par tous). */
public record ATMSoundPacket(BlockPos pos) {

    public static void encode(ATMSoundPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
    }

    public static ATMSoundPacket decode(FriendlyByteBuf buf) {
        return new ATMSoundPacket(buf.readBlockPos());
    }

    public static void handle(ATMSoundPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var sender = ctx.get().getSender();
            if (sender == null) return;
            Level level = sender.level;
            // null comme premier param → son diffusé à tous les joueurs à portée (pas seulement l'émetteur)
            level.playSound(null, packet.pos(), CucubanySounds.ATM_BIP.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        });
        ctx.get().setPacketHandled(true);
    }
}
