package fr.cucubany.cucubanymod.network;

import fr.cucubany.cucubanymod.roleplay.skin.custom.CharacterOption;
import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import fr.cucubany.cucubanymod.server.ServerSkinManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class RequestSkinContentPacket {
    private final Set<SkinPart> categories;
    private final List<String> specificIds;

    // Constructeur pour demander des catégories entières (ex: ouverture éditeur)
    public RequestSkinContentPacket(Set<SkinPart> categories) {
        this.categories = categories;
        this.specificIds = new ArrayList<>();
    }

    // Constructeur complet
    public RequestSkinContentPacket(Set<SkinPart> categories, List<String> specificIds) {
        this.categories = categories;
        this.specificIds = specificIds;
    }

    public static void encode(RequestSkinContentPacket msg, FriendlyByteBuf buf) {
        // Encode EnumSet (int bitmask est le plus efficace, mais on va faire simple : taille + noms)
        buf.writeInt(msg.categories.size());
        for (SkinPart part : msg.categories) {
            buf.writeEnum(part);
        }

        // Encode IDs
        buf.writeInt(msg.specificIds.size());
        for (String id : msg.specificIds) {
            buf.writeUtf(id);
        }
    }

    public static RequestSkinContentPacket decode(FriendlyByteBuf buf) {
        int catSize = buf.readInt();
        Set<SkinPart> categories = EnumSet.noneOf(SkinPart.class);
        for (int i = 0; i < catSize; i++) {
            categories.add(buf.readEnum(SkinPart.class));
        }

        int idSize = buf.readInt();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < idSize; i++) {
            ids.add(buf.readUtf());
        }

        return new RequestSkinContentPacket(categories, ids);
    }

    public static void handle(RequestSkinContentPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Côté SERVEUR : On reçoit la demande

            // 1. On récupère les données
            List<CharacterOption> optionsToSend = ServerSkinManager.getRequestedOptions(msg.categories, msg.specificIds);

            // 2. On prépare la réponse
            // ATTENTION : Si le paquet est trop gros (> 2Mo), il faudrait le splitter.
            // Pour l'instant on envoie tout, mais pour la prod il faudra paginer si tu as 500 coiffures.
            CustomizationOptionsPacket response = new CustomizationOptionsPacket(optionsToSend);

            // 3. On répond au joueur qui a fait la demande
            CucubanyPacketHandler.INSTANCE.sendTo(response, ctx.get().getSender().connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }
}