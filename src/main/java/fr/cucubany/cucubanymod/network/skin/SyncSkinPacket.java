package fr.cucubany.cucubanymod.network.skin;

import fr.cucubany.cucubanymod.client.skin.ClientSkinManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncSkinPacket {
    private final UUID playerUUID;
    private final byte[] skinData;
    private final boolean isSlim; // <--- Nouveau

    public SyncSkinPacket(UUID playerUUID, byte[] skinData, boolean isSlim) {
        this.playerUUID = playerUUID;
        this.skinData = skinData;
        this.isSlim = isSlim;
    }

    public static void encode(SyncSkinPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUUID);
        buf.writeInt(msg.skinData.length);
        buf.writeBytes(msg.skinData);
        buf.writeBoolean(msg.isSlim); // <--- Nouveau
    }

    public static SyncSkinPacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        int size = buf.readInt();
        byte[] data = new byte[size];
        buf.readBytes(data);
        boolean isSlim = buf.readBoolean(); // <--- Nouveau
        return new SyncSkinPacket(uuid, data, isSlim);
    }

    public static void handle(SyncSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                // On passe isSlim au manager
                ClientSkinManager.registerSkin(msg.playerUUID, msg.skinData, msg.isSlim);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}