package fr.cucubany.cucubanymod.roleplay;

import com.wildfire.main.GenderPlayer;
import net.minecraft.nbt.CompoundTag;

public class GenderOption {

    private GenderPlayer.Gender gender = GenderPlayer.Gender.MALE;
    private float bustSize = 0.6f;
    private float xOffset = 0.0f;
    private float yOffset = 0.0f;
    private float zOffset = 0.0f;
    private float cleavage = 0.05f;
    private float bounceMultiplier = 0.34f;
    private float floppyMultiplier = 0.95f;
    private boolean breastPhysics = false;
    private boolean breastPhysicsArmor = false;
    private boolean showInArmor = true;
    private boolean hurtSounds = true;
    private boolean uniboob = false;
    private boolean voice = true;

    public GenderOption() {}

    // --- Getters ---

    public GenderPlayer.Gender getGender() { return gender; }
    public float getBustSize() { return bustSize; }
    public float getXOffset() { return xOffset; }
    public float getYOffset() { return yOffset; }
    public float getZOffset() { return zOffset; }
    public float getCleavage() { return cleavage; }
    public float getBounceMultiplier() { return bounceMultiplier; }
    public float getFloppyMultiplier() { return floppyMultiplier; }
    public boolean hasBreastPhysics() { return breastPhysics; }
    public boolean hasBreastPhysicsArmor() { return breastPhysicsArmor; }
    public boolean isShowInArmor() { return showInArmor; }
    public boolean hasHurtSounds() { return hurtSounds; }
    public boolean isUniboob() { return uniboob; }
    public boolean isVoiceFeminine() { return voice; }

    // --- Setters ---

    public void setGender(GenderPlayer.Gender gender) { this.gender = gender; }
    public void setBustSize(float bustSize) { this.bustSize = bustSize; }
    public void setVoice(boolean voice) { this.voice = voice; }

    public GenderPlayer.Gender nextGender() {
        return switch (gender) {
            case MALE -> GenderPlayer.Gender.FEMALE;
            case FEMALE -> GenderPlayer.Gender.OTHER;
            default -> GenderPlayer.Gender.MALE;
        };
    }

    /**
     * Applique les données de poitrine au GenderPlayer Wildfire.
     */
    public void applyTo(GenderPlayer wPlayer) {
        wPlayer.updateGender(gender);
        wPlayer.updateBustSize(bustSize);
        wPlayer.updateBounceMultiplier(bounceMultiplier);
        wPlayer.updateFloppiness(floppyMultiplier);
        wPlayer.updateBreastPhysics(breastPhysics);
        wPlayer.updateArmorBreastPhysics(breastPhysicsArmor);
        wPlayer.updateShowBreastsInArmor(showInArmor);
        wPlayer.updateHurtSounds(hurtSounds);
        wPlayer.getBreasts().updateXOffset(xOffset);
        wPlayer.getBreasts().updateYOffset(yOffset);
        wPlayer.getBreasts().updateZOffset(zOffset);
        wPlayer.getBreasts().updateCleavage(cleavage);
        wPlayer.getBreasts().updateUniboob(uniboob);
    }

    // --- Sérialisation NBT ---

    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("gender", gender.name());
        tag.putFloat("bustSize", bustSize);
        tag.putFloat("xOffset", xOffset);
        tag.putFloat("yOffset", yOffset);
        tag.putFloat("zOffset", zOffset);
        tag.putFloat("cleavage", cleavage);
        tag.putFloat("bounceMultiplier", bounceMultiplier);
        tag.putFloat("floppyMultiplier", floppyMultiplier);
        tag.putBoolean("breastPhysics", breastPhysics);
        tag.putBoolean("breastPhysicsArmor", breastPhysicsArmor);
        tag.putBoolean("showInArmor", showInArmor);
        tag.putBoolean("hurtSounds", hurtSounds);
        tag.putBoolean("uniboob", uniboob);
        tag.putBoolean("voice", voice);
        return tag;
    }

    public void readNBT(CompoundTag tag) {
        try {
            gender = GenderPlayer.Gender.valueOf(tag.getString("gender"));
        } catch (Exception e) {
            gender = GenderPlayer.Gender.MALE;
        }
        bustSize = tag.getFloat("bustSize");
        xOffset = tag.getFloat("xOffset");
        yOffset = tag.getFloat("yOffset");
        zOffset = tag.getFloat("zOffset");
        cleavage = tag.contains("cleavage") ? tag.getFloat("cleavage") : 0.05f;
        bounceMultiplier = tag.contains("bounceMultiplier") ? tag.getFloat("bounceMultiplier") : 0.34f;
        floppyMultiplier = tag.contains("floppyMultiplier") ? tag.getFloat("floppyMultiplier") : 0.95f;
        breastPhysics = tag.getBoolean("breastPhysics");
        breastPhysicsArmor = tag.getBoolean("breastPhysicsArmor");
        showInArmor = tag.contains("showInArmor") ? tag.getBoolean("showInArmor") : true;
        hurtSounds = tag.contains("hurtSounds") ? tag.getBoolean("hurtSounds") : true;
        uniboob = tag.getBoolean("uniboob");
        voice = tag.contains("voice") ? tag.getBoolean("voice") : true;
    }
}