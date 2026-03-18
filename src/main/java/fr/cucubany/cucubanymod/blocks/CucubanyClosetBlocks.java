package fr.cucubany.cucubanymod.blocks;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.blocks.clothing.BigClosetBlock;
import fr.cucubany.cucubanymod.blocks.clothing.ClassicClosetBlock;
import fr.cucubany.cucubanymod.blocks.clothing.ShoeboxClosetBlock;
import fr.cucubany.cucubanymod.items.CucubanyCreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;

import static fr.cucubany.cucubanymod.blocks.CucubanyBlocks.registerBlock;

public class CucubanyClosetBlocks {

    public static void init() {
        CucubanyMod.getLogger().info("Registering Closet Blocks");
    }

    public static final RegistryObject<Block> CLOSET_BIRCH = registerClassicCloset("birch");
    public static final RegistryObject<Block> CLOSET_BLACK = registerClassicCloset("black");
    public static final RegistryObject<Block> CLOSET_CHERRY = registerClassicCloset("cherry");
    public static final RegistryObject<Block> CLOSET_EBONY = registerClassicCloset("ebony");
    public static final RegistryObject<Block> CLOSET_OAK = registerClassicCloset("oak");
    public static final RegistryObject<Block> CLOSET_PALM = registerClassicCloset("palm");
    public static final RegistryObject<Block> CLOSET_SPRUCE = registerClassicCloset("spruce");
    public static final RegistryObject<Block> CLOSET_WHITE = registerClassicCloset("white");

    public static final RegistryObject<Block> SHOEBOX_BIRCH = registerShoeboxCloset("birch");
    public static final RegistryObject<Block> SHOEBOX_BLACK = registerShoeboxCloset("black");
    public static final RegistryObject<Block> SHOEBOX_CHERRY = registerShoeboxCloset("cherry");
    public static final RegistryObject<Block> SHOEBOX_EBONY = registerShoeboxCloset("ebony");
    public static final RegistryObject<Block> SHOEBOX_OAK = registerShoeboxCloset("oak");
    public static final RegistryObject<Block> SHOEBOX_PALM = registerShoeboxCloset("palm");
    public static final RegistryObject<Block> SHOEBOX_SPRUCE = registerShoeboxCloset("spruce");
    public static final RegistryObject<Block> SHOEBOX_WHITE = registerShoeboxCloset("white");

    public static final RegistryObject<Block> BIG_CLOSET_BIRCH  = registerBigCloset("birch");
    public static final RegistryObject<Block> BIG_CLOSET_BLACK  = registerBigCloset("black");
    public static final RegistryObject<Block> BIG_CLOSET_CHERRY = registerBigCloset("cherry");
    public static final RegistryObject<Block> BIG_CLOSET_EBONY  = registerBigCloset("ebony");
    public static final RegistryObject<Block> BIG_CLOSET_OAK    = registerBigCloset("oak");
    public static final RegistryObject<Block> BIG_CLOSET_PALM   = registerBigCloset("palm");
    public static final RegistryObject<Block> BIG_CLOSET_SPRUCE = registerBigCloset("spruce");
    public static final RegistryObject<Block> BIG_CLOSET_WHITE  = registerBigCloset("white");


    private static RegistryObject<Block> registerClassicCloset(String variant) {
        return registerBlock("closet_" + variant, () ->
                        new ClassicClosetBlock(BlockBehaviour.Properties
                                .of(Material.WOOD)
                                .noOcclusion()
                        ),
                CucubanyCreativeModeTab.SKINS_TAB);
    }

    private static RegistryObject<Block> registerShoeboxCloset(String variant) {
        return registerBlock(("shoebox_" + variant), () ->
                new ShoeboxClosetBlock(BlockBehaviour.Properties
                        .of(Material.WOOD)
                        .noOcclusion()
                ),
                CucubanyCreativeModeTab.SKINS_TAB);
    }

    private static RegistryObject<Block> registerBigCloset(String variant) {
        return registerBlock("big_closet_" + variant, () ->
                new BigClosetBlock(BlockBehaviour.Properties
                        .of(Material.WOOD)
                        .noOcclusion()
                ),
                CucubanyCreativeModeTab.SKINS_TAB);
    }
}
