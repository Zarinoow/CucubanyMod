package fr.cucubany.cucubanymod.commands;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCommands {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ResetIdentityCommand.register(event.getServer().getCommands().getDispatcher());
        GetIdentityCommand.register(event.getServer().getCommands().getDispatcher());
        SkillCommand.register(event.getServer().getCommands().getDispatcher());
    }

}
