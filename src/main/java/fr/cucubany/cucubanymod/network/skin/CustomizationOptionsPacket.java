package fr.cucubany.cucubanymod.network.skin;

import fr.cucubany.cucubanymod.roleplay.skin.custom.CharacterOption;
import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record CustomizationOptionsPacket(List<CharacterOption> options) {

    public static void encode(CustomizationOptionsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.options.size());
        for (CharacterOption option : msg.options) {
            buf.writeUtf(option.id());
            buf.writeUtf(option.category().name());
            byte[] textureData = option.textureData();
            buf.writeInt(textureData.length); // Envoi de la taille des données
            buf.writeBytes(textureData); // Envoi des données elles-mêmes
        }
    }

    public static CustomizationOptionsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<CharacterOption> options = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String id = buf.readUtf();
            String category = buf.readUtf();
            int textureSize = buf.readInt(); // Taille des données de la texture
            byte[] textureData = new byte[textureSize];
            buf.readBytes(textureData); // Lecture des données de la texture
            options.add(new CharacterOption(id, SkinPart.valueOf(category), textureData));
        }
        return new CustomizationOptionsPacket(options);
    }
}
