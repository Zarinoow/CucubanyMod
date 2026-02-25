package fr.cucubany.cucubanymod.roleplay.skin;

import com.wildfire.main.GenderPlayer;
import fr.cucubany.cucubanymod.roleplay.skin.custom.SkinPart;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class CharacterAppearance {
    private boolean isSlim;
    private GenderPlayer.Gender gender;
    private float bustSize;
    private float bustXOffset;
    private float bustYOffset;
    private float bustZOffset;
    private float bustCleavage;
    // Map <Partie, ID du fichier>
    private final Map<SkinPart, String> selectedParts;
    // Map <Partie, Couleur Int>
    private final Map<SkinPart, Integer> partColors;

    public CharacterAppearance(boolean isSlim, GenderPlayer.Gender gender,
                               float bustSize, float bustXOffset, float bustYOffset,
                               float bustZOffset, float bustCleavage,
                               Map<SkinPart, String> selectedParts, Map<SkinPart, Integer> partColors) {
        this.isSlim = isSlim;
        this.gender = gender;
        this.bustSize = bustSize;
        this.bustXOffset = bustXOffset;
        this.bustYOffset = bustYOffset;
        this.bustZOffset = bustZOffset;
        this.bustCleavage = bustCleavage;
        this.selectedParts = selectedParts;
        this.partColors = partColors;
    }

    public boolean isSlim() { return isSlim; }
    public GenderPlayer.Gender getGender() { return gender; }
    public float getBustSize() { return bustSize; }
    public float getBustXOffset() { return bustXOffset; }
    public float getBustYOffset() { return bustYOffset; }
    public float getBustZOffset() { return bustZOffset; }
    public float getBustCleavage() { return bustCleavage; }
    public Map<SkinPart, String> getSelectedParts() { return selectedParts; }
    public Map<SkinPart, Integer> getPartColors() { return partColors; }

    // --- ENCODAGE RÉSEAU ---

    public static void encode(CharacterAppearance msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isSlim);
        buf.writeEnum(msg.gender);
        buf.writeFloat(msg.bustSize);
        buf.writeFloat(msg.bustXOffset);
        buf.writeFloat(msg.bustYOffset);
        buf.writeFloat(msg.bustZOffset);
        buf.writeFloat(msg.bustCleavage);

        buf.writeInt(msg.selectedParts.size());
        msg.selectedParts.forEach((part, id) -> {
            buf.writeEnum(part);
            buf.writeUtf(id);
        });

        buf.writeInt(msg.partColors.size());
        msg.partColors.forEach((part, color) -> {
            buf.writeEnum(part);
            buf.writeInt(color);
        });
    }

    public static CharacterAppearance decode(FriendlyByteBuf buf) {
        boolean slim = buf.readBoolean();
        GenderPlayer.Gender gender = buf.readEnum(GenderPlayer.Gender.class);
        float bustSize = buf.readFloat();
        float bustXOffset = buf.readFloat();
        float bustYOffset = buf.readFloat();
        float bustZOffset = buf.readFloat();
        float bustCleavage = buf.readFloat();

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

        return new CharacterAppearance(slim, gender, bustSize, bustXOffset, bustYOffset, bustZOffset, bustCleavage, parts, colors);
    }
}