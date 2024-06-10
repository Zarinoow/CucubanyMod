package fr.cucubany.cucubanymod.armor;

import fr.cucubany.cucubanymod.CucubanyMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import zombie_extreme.init.ZombieExtremeModItems;

public enum HazmatMaterial implements ArmorMaterial {

    HELMET(13, 2),
    CHESTPLATE(15, 5),
    LEGGINGS(16, 6),
    BOOTS(11, 2);

    private static final String name = CucubanyMod.MOD_ID + ":hazmat";
    private static final int enchantment = 9;
    private static final SoundEvent sound = SoundEvents.ARMOR_EQUIP_LEATHER;
    private static final Ingredient ingredient = Ingredient.of(ZombieExtremeModItems.LEAD_PLATE.get());
    private final int durability;
    private final int defense;

    HazmatMaterial(int durability, int defense) {
        this.durability = durability * 40;
        this.defense = defense;
    }


    @Override
    public int getDurabilityForSlot(EquipmentSlot pSlot) {
        return durability;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot pSlot) {
        return defense;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantment;
    }

    @Override
    public SoundEvent getEquipSound() {
        return sound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ingredient;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}
