package fr.cucubany.cucubanymod.roleplay.skin;

import com.wildfire.main.GenderPlayer;
import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class CharacterAppearance {
    private boolean isSlim;
    private GenderPlayer.Gender gender;
    // Map <Partie, ID du fichier>
    private final Map<SkinPart, String> selectedParts;
    // Map <Partie, Couleur Int>
    private final Map<SkinPart, Integer> partColors;

    public CharacterAppearance(boolean isSlim, GenderPlayer.Gender gender, Map<SkinPart, String> selectedParts, Map<SkinPart, Integer> partColors) {
        this.isSlim = isSlim;
        this.gender = gender;
        this.selectedParts = selectedParts;
        this.partColors = partColors;
    }

    public boolean isSlim() { return isSlim; }
    public GenderPlayer.Gender getGender() { return gender; }
    public Map<SkinPart, String> getSelectedParts() { return selectedParts; }
    public Map<SkinPart, Integer> getPartColors() { return partColors; }

    // --- ENCODAGE RÉSEAU ---

    public static void encode(CharacterAppearance msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isSlim);
        buf.writeEnum(msg.gender);

        // Taille des parts
        buf.writeInt(msg.selectedParts.size());
        msg.selectedParts.forEach((part, id) -> {
            buf.writeEnum(part);
            buf.writeUtf(id);
        });

        // Taille des couleurs
        buf.writeInt(msg.partColors.size());
        msg.partColors.forEach((part, color) -> {
            buf.writeEnum(part);
            buf.writeInt(color);
        });
    }

    public static CharacterAppearance decode(FriendlyByteBuf buf) {
        boolean slim = buf.readBoolean();
        GenderPlayer.Gender gender = buf.readEnum(GenderPlayer.Gender.class);

        int partsSize = buf.readInt();
        Map<SkinPart, String> parts = new HashMap<>();
        for (int i = 0; i < partsSize; i++) {
            parts.put(buf.readEnum(SkinPart.class), buf.readUtf());
        }

        int colorsSize = buf.readInt();
        Map<SkinPart, Integer> colors = new HashMap<>();
        for (int i = 0; i < colorsSize; i++) {
            colors.put(buf.readEnum(SkinPart.class), buf.readInt());
        }

        return new CharacterAppearance(slim, gender, parts, colors);
    }
}