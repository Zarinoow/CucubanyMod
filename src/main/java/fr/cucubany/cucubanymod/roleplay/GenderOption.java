package fr.cucubany.cucubanymod.roleplay;

import com.wildfire.main.GenderPlayer;

public class GenderOption {

    // Default values


    private static final boolean uniboob = false;

    private static final boolean breasts_physics = false;
    private static final boolean breasts_physics_armor = false;
    private static final boolean show_in_armor = true;
    private static final float bounce_multiplier = 0.34f;
    private static final float floppy_multiplier = 0.95f;

    // Gender options
    private GenderPlayer.Gender gender;
    private float breast = 0.6f; // Breast size
    private float breasts_offset_x = 0.0f; // Separation
    private float breasts_offset_y = 0.0f; // Height
    private float breasts_offset_z = 0.0f; // Depth
    private float breasts_cleavage = 0.05f; // Rotation


    private boolean voice = true; // true = feminine, false = masculine

    public void setGender(GenderPlayer.Gender gender) {
        this.gender = gender;
    }

    public void setVoice(boolean voice) {
        this.voice = voice;
    }

    public GenderPlayer.Gender getGender() {
        return gender;
    }

    public GenderPlayer.Gender nextGender() {
        return switch (gender) {
            case MALE -> GenderPlayer.Gender.FEMALE;
            case FEMALE -> GenderPlayer.Gender.OTHER;
            default -> GenderPlayer.Gender.MALE;
        };
    }

    public boolean isVoiceFeminine() {
        return voice;
    }


}
