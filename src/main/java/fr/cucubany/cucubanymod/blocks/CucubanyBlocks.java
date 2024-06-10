package fr.cucubany.cucubanymod.blocks;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.blocks.advanced.VentDoorBlock;
import fr.cucubany.cucubanymod.items.CucubanyCreativeModeTab;
import fr.cucubany.cucubanymod.items.CucubanyItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class CucubanyBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CucubanyMod.MOD_ID);

    private static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> registryObject = BLOCKS.register(name, block);
        registerBlockItem(name, registryObject, tab);
        return registryObject;
    }

    private static <T extends Block>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab) {
        return CucubanyItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    /*
     * Register blocks
     */
    public static final RegistryObject<Block> VENT_BLOCK = registerBlock("vent_block", () ->
            new Block(BlockBehaviour.Properties
                    .of(Material.METAL)
                    .strength(9f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()),
            CucubanyCreativeModeTab.CUCUBANY_TAB);

    public static final RegistryObject<Block> VENT_DOOR = registerBlock("vent_door", () ->
            new VentDoorBlock(BlockBehaviour.Properties
                    .of(Material.METAL)
                    .strength(9f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()),
            CucubanyCreativeModeTab.CUCUBANY_TAB);


}
