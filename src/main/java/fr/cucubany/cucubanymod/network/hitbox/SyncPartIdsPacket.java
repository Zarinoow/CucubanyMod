package fr.cucubany.cucubanymod.network.hitbox;

import fr.cucubany.cucubanymod.hitbox.BodyPartEntity;
import fr.cucubany.cucubanymod.hitbox.IMultiPartPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncPartIdsPacket {

    private final int parentId;
    private final int[] partIds; // Liste ordonnée des IDs serveur

    public SyncPartIdsPacket(int parentId, BodyPartEntity[] parts) {
        this.parentId = parentId;
        this.partIds = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            this.partIds[i] = parts[i].getId();
        }
    }

    // Constructeur interne pour le décodage
    private SyncPartIdsPacket(int parentId, int[] partIds) {
        this.parentId = parentId;
        this.partIds = partIds;
    }

    public static SyncPartIdsPacket decode(FriendlyByteBuf buffer) {
        int parentId = buffer.readInt();
        int count = buffer.readInt();
        int[] ids = new int[count];
        for (int i = 0; i < count; i++) {
            ids[i] = buffer.readInt();
        }
        return new SyncPartIdsPacket(parentId, ids);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(parentId);
        buffer.writeInt(partIds.length);
        for (int id : partIds) {
            buffer.writeInt(id);
        }
    }

    public static void handle(SyncPartIdsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // CÔTÉ CLIENT
            if (Minecraft.getInstance().level == null) return;
            Entity parent = Minecraft.getInstance().level.getEntity(msg.parentId);

            if (parent instanceof IMultiPartPlayer multipartPlayer) {
                BodyPartEntity[] localParts = multipartPlayer.getBodyParts();

                if (localParts != null && localParts.length == msg.partIds.length) {
                    for (int i = 0; i < localParts.length; i++) {
                        // MAGIE : On force l'ID du client à devenir celui du serveur
                        localParts[i].setId(msg.partIds[i]);
                    }
                    // Debug
                    System.out.println("IDs Synchronisés pour " + parent.getName().getString());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
