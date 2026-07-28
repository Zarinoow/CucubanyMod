package fr.cucubany.cucubanymod.network.hitbox;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.client.network.ClientPacketHandlers;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class SyncBodyHealthPacket {

    private final CompoundTag data;

    // Constructeur pour l'envoi (Serveur -> Buffer)
    public SyncBodyHealthPacket(CompoundTag data) {
        this.data = data;
    }

    // Décodage (Buffer -> Paquet)
    public static SyncBodyHealthPacket decode(FriendlyByteBuf buffer) {
        return new SyncBodyHealthPacket(buffer.readNbt());
    }

    // Encodage (Paquet -> Buffer)
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(this.data);
    }

    // Réception (Client)
    public static void handle(SyncBodyHealthPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.applyBodyHealth(msg.data))
        );
        CucubanyMod.getLogger().info("[DEBUG] Received SyncBodyHealthPacket with data: " + msg.data);
        ctx.get().setPacketHandled(true);
    }

    public static void sendHealthSync(ServerPlayer player, CompoundTag nbt) {
        CucubanyPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncBodyHealthPacket(nbt));
    }
}