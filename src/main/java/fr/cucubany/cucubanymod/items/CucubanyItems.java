package fr.cucubany.cucubanymod.items;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.armor.HazmatSuit;
import fr.cucubany.cucubanymod.items.advanced.IdentityCardItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CucubanyItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CucubanyMod.MOD_ID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    /*
     * Register items
     */
    public static final RegistryObject<Item> IDENTITY_CARD = ITEMS.register("id_card", () -> new IdentityCardItem(new Item.Properties()
            .tab(CucubanyCreativeModeTab.CUCUBANY_TAB)
            .stacksTo(1)));
    public static final RegistryObject<Item> SCREWDRIVER = ITEMS.register("screwdriver", () -> new Item(new Item.Properties()
            .tab(CucubanyCreativeModeTab.CUCUBANY_TAB)
            .stacksTo(1)
            .defaultDurability(20)));
    public static final RegistryObject<Item> URANIUM_ROD = ITEMS.register("uranium_rod", () -> new Item(new Item.Properties()
            .tab(CucubanyCreativeModeTab.CUCUBANY_TAB)));
    public static final RegistryObject<Item> URANIUM_USED_ROD = ITEMS.register("uranium_used_rod", () -> new Item(new Item.Properties()
            .tab(CucubanyCreativeModeTab.CUCUBANY_TAB)));

    /*
     * Register armor
     */
    public static final RegistryObject<Item> HAZMAT_SUIT_HELMET = ITEMS.register("hazmat_suit_helmet", () -> new HazmatSuit.Helmet());
    public static final RegistryObject<Item> HAZMAT_SUIT_CHESTPLATE = ITEMS.register("hazmat_suit_chestplate", () -> new HazmatSuit.Chestplate());
    public static final RegistryObject<Item> HAZMAT_SUIT_LEGGINGS = ITEMS.register("hazmat_suit_leggings", () -> new HazmatSuit.Leggings());
    public static final RegistryObject<Item> HAZMAT_SUIT_BOOTS = ITEMS.register("hazmat_suit_boots", () -> new HazmatSuit.Boots());


}
