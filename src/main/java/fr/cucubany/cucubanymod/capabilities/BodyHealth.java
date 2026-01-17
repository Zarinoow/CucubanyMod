package fr.cucubany.cucubanymod.capabilities;

import fr.cucubany.cucubanymod.hitbox.BodyPart;
import net.minecraft.nbt.CompoundTag;
import java.util.EnumMap;
import java.util.Map;

public class BodyHealth implements IBodyHealth {

    private final Map<BodyPart, Float> healthMap = new EnumMap<>(BodyPart.class);
    private static final float MAX_HEALTH = 20.0F; // Vie standard par membre

    public BodyHealth() {
        this.reset();
    }

    @Override
    public float getHealth(BodyPart part) {
        return healthMap.getOrDefault(part, MAX_HEALTH);
    }

    @Override
    public void setHealth(BodyPart part, float amount) {
        // Clamp entre 0 et Max
        float value = Math.max(0, Math.min(amount, getMaxHealth(part)));
        healthMap.put(part, value);
    }

    @Override
    public void hurt(BodyPart part, float amount) {
        setHealth(part, getHealth(part) - amount);
    }

    @Override
    public void heal(BodyPart part, float amount) {
        setHealth(part, getHealth(part) + amount);
    }

    @Override
    public float getMaxHealth(BodyPart part) {
        // On pourrait varier ici (ex: Tête = 10PV, Torse = 20PV)
        return MAX_HEALTH;
    }

    @Override
    public void reset() {
        for (BodyPart part : BodyPart.values()) {
            healthMap.put(part, getMaxHealth(part));
        }
    }

    // --- Sauvegarde (NBT) ---
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<BodyPart, Float> entry : healthMap.entrySet()) {
            tag.putFloat(entry.getKey().name(), entry.getValue());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (BodyPart part : BodyPart.values()) {
            if (nbt.contains(part.name())) {
                healthMap.put(part, nbt.getFloat(part.name()));
            }
        }
    }
}
