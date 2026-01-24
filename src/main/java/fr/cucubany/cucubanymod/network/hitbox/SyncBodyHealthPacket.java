package fr.cucubany.cucubanymod.network.hitbox;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.capabilities.BodyHealthProvider;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;

            if (player != null) {
                player.getCapability(BodyHealthProvider.BODY_HEALTH_CAPABILITY).ifPresent(cap -> {
                    cap.deserializeNBT(msg.data);
                });
            }
        });
        CucubanyMod.getLogger().info("[DEBUG] Received SyncBodyHealthPacket with data: " + msg.data);
        ctx.get().setPacketHandled(true);
    }

    public static void sendHealthSync(ServerPlayer player, CompoundTag nbt) {
        CucubanyPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncBodyHealthPacket(nbt));
    }
}