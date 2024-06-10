package fr.cucubany.cucubanymod.armor;

import fr.cucubany.cucubanymod.items.CucubanyCreativeModeTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import zombie_extreme.ZombieExtremeMod;
import zombie_extreme.client.model.ModelBoots_Hazmat_Suit;
import zombie_extreme.client.model.ModelChestplate_Hazmat_Suit;
import zombie_extreme.client.model.ModelHelmet_Hazmat_Suit;
import zombie_extreme.client.model.ModelLeggings_Hazmat_Suit;
import zombie_extreme.init.ZombieExtremeModSounds;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class HazmatSuit {

    public static boolean isWearingFullHazmatSuit(Player player) {
        if (player.getInventory().armor.size() != 4) return false;
        return player.getInventory().armor.get(0).getItem() instanceof HazmatSuit.Boots
                && player.getInventory().armor.get(1).getItem() instanceof HazmatSuit.Leggings
                && player.getInventory().armor.get(2).getItem() instanceof HazmatSuit.Chestplate
                && player.getInventory().armor.get(3).getItem() instanceof HazmatSuit.Helmet;
    }

    public static class Helmet extends ArmorItem {
        public Helmet() {
            super(HazmatMaterial.HELMET, EquipmentSlot.HEAD, new Item.Properties()
                    .tab(CucubanyCreativeModeTab.CUCUBANY_TAB)
                    .stacksTo(1));
        }

        @Override
        public void initializeClient(Consumer<IItemRenderProperties> consumer) {
            consumer.accept(new IItemRenderProperties() {
                public HumanoidModel getArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
                    HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(), Map.of("head", (new ModelHelmet_Hazmat_Suit(Minecraft.getInstance().getEntityModels().bakeLayer(ModelHelmet_Hazmat_Suit.LAYER_LOCATION))).Head, "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                    armorModel.crouching = living.isShiftKeyDown();
                    armorModel.riding = defaultModel.riding;
                    armorModel.young = living.isBaby();
                    return armorModel;
                }
            });
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "zombie_extreme:textures/entities/helmet_hazmat_suit.png";
        }

        @Override
        public void onArmorTick(ItemStack stack, Level level, Player player) {
            level.playSound(null, player.getOnPos(), new SoundEvent(new ResourceLocation(ZombieExtremeMod.MODID, "gas_mask_sound_effect")), SoundSource.PLAYERS, 0.2f, 1.0f);
        }
    }

    public static class Chestplate extends ArmorItem {
        public Chestplate() {
            super(HazmatMaterial.CHESTPLATE, EquipmentSlot.CHEST, new Item.Properties()
                    .tab(CucubanyCreativeModeTab.CUCUBANY_TAB)
                    .stacksTo(1));
        }

        @Override
        public void initializeClient(Consumer<IItemRenderProperties> consumer) {
            consumer.accept(new IItemRenderProperties() {
                @OnlyIn(Dist.CLIENT)
                public HumanoidModel getArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
                    HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(), Map.of("body", (new ModelChestplate_Hazmat_Suit(Minecraft.getInstance().getEntityModels().bakeLayer(ModelChestplate_Hazmat_Suit.LAYER_LOCATION))).Body, "left_arm", (new ModelChestplate_Hazmat_Suit(Minecraft.getInstance().getEntityModels().bakeLayer(ModelChestplate_Hazmat_Suit.LAYER_LOCATION))).LArm, "right_arm", (new ModelChestplate_Hazmat_Suit(Minecraft.getInstance().getEntityModels().bakeLayer(ModelChestplate_Hazmat_Suit.LAYER_LOCATION))).RArm, "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                    armorModel.crouching = living.isShiftKeyDown();
                    armorModel.riding = defaultModel.riding;
                    armorModel.young = living.isBaby();
                    return armorModel;
                }
            });
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "zombie_extreme:textures/entities/chestplate_hazmat_suit.png";
        }
    }

    public static class Leggings extends ArmorItem {
        public Leggings() {
            super(HazmatMaterial.LEGGINGS, EquipmentSlot.LEGS, new Item.Properties()
                    .tab(CucubanyCreativeModeTab.CUCUBANY_TAB)
                    .stacksTo(1));
        }

        @Override
        public void initializeClient(Consumer<IItemRenderProperties> consumer) {
            consumer.accept(new IItemRenderProperties() {
                @OnlyIn(Dist.CLIENT)
                public HumanoidModel getArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
                    HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(), Map.of("left_leg", (new ModelLeggings_Hazmat_Suit(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLeggings_Hazmat_Suit.LAYER_LOCATION))).LLeg, "right_leg", (new ModelLeggings_Hazmat_Suit(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLeggings_Hazmat_Suit.LAYER_LOCATION))).RLeg, "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                    armorModel.crouching = living.isShiftKeyDown();
                    armorModel.riding = defaultModel.riding;
                    armorModel.young = living.isBaby();
                    return armorModel;
                }
            });
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "zombie_extreme:textures/entities/leggings_hazmat_suit.png";
        }
    }

    public static class Boots extends ArmorItem {
        public Boots() {
            super(HazmatMaterial.BOOTS, EquipmentSlot.FEET, new Item.Properties()
                    .tab(CucubanyCreativeModeTab.CUCUBANY_TAB)
                    .stacksTo(1));
        }

        @Override
        public void initializeClient(Consumer<IItemRenderProperties> consumer) {
            consumer.accept(new IItemRenderProperties() {
                @OnlyIn(Dist.CLIENT)
                public HumanoidModel getArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
                    HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(), Map.of("left_leg", (new ModelBoots_Hazmat_Suit(Minecraft.getInstance().getEntityModels().bakeLayer(ModelBoots_Hazmat_Suit.LAYER_LOCATION))).LBoot, "right_leg", (new ModelBoots_Hazmat_Suit(Minecraft.getInstance().getEntityModels().bakeLayer(ModelBoots_Hazmat_Suit.LAYER_LOCATION))).RBoot, "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                    armorModel.crouching = living.isShiftKeyDown();
                    armorModel.riding = defaultModel.riding;
                    armorModel.young = living.isBaby();
                    return armorModel;
                }
            });
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "zombie_extreme:textures/entities/boots_hazmat_suit.png";
        }
    }

}
