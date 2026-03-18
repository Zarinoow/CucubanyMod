package fr.cucubany.cucubanymod.blocks;

import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.blocks.clothing.BigClosetBlockEntity;
import fr.cucubany.cucubanymod.blocks.clothing.ClosetBlockEntity;
import fr.cucubany.cucubanymod.blocks.clothing.DoubleClosetBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CucubanyBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, CucubanyMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<ClosetBlockEntity>> CLOSET_SIMPLE =
            BLOCK_ENTITIES.register("closet_simple", () ->
                    BlockEntityType.Builder.of(ClosetBlockEntity::new,
                            CucubanyClosetBlocks.SHOEBOX_BIRCH.get(),
                            CucubanyClosetBlocks.SHOEBOX_BLACK.get(),
                            CucubanyClosetBlocks.SHOEBOX_CHERRY.get(),
                            CucubanyClosetBlocks.SHOEBOX_EBONY.get(),
                            CucubanyClosetBlocks.SHOEBOX_OAK.get(),
                            CucubanyClosetBlocks.SHOEBOX_PALM.get(),
                            CucubanyClosetBlocks.SHOEBOX_SPRUCE.get(),
                            CucubanyClosetBlocks.SHOEBOX_WHITE.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<DoubleClosetBlockEntity>> CLOSET_DOUBLE =
            BLOCK_ENTITIES.register("closet_double", () ->
                    BlockEntityType.Builder.of(DoubleClosetBlockEntity::new,
                            CucubanyClosetBlocks.CLOSET_BIRCH.get(),
                            CucubanyClosetBlocks.CLOSET_BLACK.get(),
                            CucubanyClosetBlocks.CLOSET_CHERRY.get(),
                            CucubanyClosetBlocks.CLOSET_EBONY.get(),
                            CucubanyClosetBlocks.CLOSET_OAK.get(),
                            CucubanyClosetBlocks.CLOSET_PALM.get(),
                            CucubanyClosetBlocks.CLOSET_SPRUCE.get(),
                            CucubanyClosetBlocks.CLOSET_WHITE.get()
                            ).build(null));

    public static final RegistryObject<BlockEntityType<BigClosetBlockEntity>> CLOSET_BIG =
            BLOCK_ENTITIES.register("closet_big", () ->
                    BlockEntityType.Builder.of(BigClosetBlockEntity::new,
                            CucubanyClosetBlocks.BIG_CLOSET_BIRCH.get(),
                            CucubanyClosetBlocks.BIG_CLOSET_BLACK.get(),
                            CucubanyClosetBlocks.BIG_CLOSET_CHERRY.get(),
                            CucubanyClosetBlocks.BIG_CLOSET_EBONY.get(),
                            CucubanyClosetBlocks.BIG_CLOSET_OAK.get(),
                            CucubanyClosetBlocks.BIG_CLOSET_PALM.get(),
                            CucubanyClosetBlocks.BIG_CLOSET_SPRUCE.get(),
                            CucubanyClosetBlocks.BIG_CLOSET_WHITE.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}