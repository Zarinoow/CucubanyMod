package fr.cucubany.cucubanymod;

import com.mojang.logging.LogUtils;
import fr.cucubany.cucubanymod.blocks.CucubanyBlocks;
import fr.cucubany.cucubanymod.client.keybind.KeyBinding;
import fr.cucubany.cucubanymod.commands.RegisterCommands;
import fr.cucubany.cucubanymod.config.CucubanyClientConfigs;
import fr.cucubany.cucubanymod.config.CucubanyCommonConfigs;
import fr.cucubany.cucubanymod.config.CucubanyServerConfigs;
import fr.cucubany.cucubanymod.events.CapabilitiesSubscriber;
import fr.cucubany.cucubanymod.events.DeathEventSubscriber;
import fr.cucubany.cucubanymod.items.CucubanyItems;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.sounds.CucubanySounds;
import fr.cucubany.cucubanymod.world.biome.CucubanyBiomes;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CucubanyMod.MOD_ID)
public class CucubanyMod
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "cucubanymod";

    public CucubanyMod()
    {
        // Register the setup method for modloading
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CucubanyCommonConfigs.COMMON_SPEC, "cucubany-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CucubanyClientConfigs.CLIENT_SPEC, "cucubany-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CucubanyServerConfigs.SERVER_SPEC, "cucubany-server.toml");


        // Load the mod
        CucubanyBiomes.register(modEventBus);
        CucubanyItems.register(modEventBus);
        CucubanyBlocks.register(modEventBus);
        CucubanySounds.register(modEventBus);

        modEventBus.addListener(this::clientSetup);

        // Register the packet handler
        CucubanyPacketHandler.register();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // DeathEventSubscriber is a webhook that sends a message to a Discord channel when a player dies
        MinecraftForge.EVENT_BUS.register(new DeathEventSubscriber());

        // Register capabilities
        MinecraftForge.EVENT_BUS.register(CapabilitiesSubscriber.class);

        // Register commands
        MinecraftForge.EVENT_BUS.register(RegisterCommands.class);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CucubanyBlocks.VENT_BLOCK.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(CucubanyBlocks.VENT_DOOR.get(), RenderType.cutout());
        KeyBinding.register();
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
