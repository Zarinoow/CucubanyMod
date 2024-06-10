package fr.cucubany.cucubanymod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Collection;

public class ResetIdentityCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal("identity")
                .then(Commands.literal("reset")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ResetIdentityCommand::execute)))
                .requires(cs -> cs.hasPermission(2)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");

        if(players.isEmpty()) {
            return 0;
        }

        players.forEach(ResetIdentityCommand::resetIdentity);

        context.getSource().sendSuccess(new TranslatableComponent("message.cucubanymod.identity.reset.sucess", players.size()), true);
        return 1;
    }

    private static void resetIdentity(ServerPlayer player) {
        LazyOptional<IIdentityCapability> identityCap = player.getCapability(IdentityCapabilityProvider.IDENTITY_CAPABILITY);

        identityCap.ifPresent(cap -> {
            cap.setIdentity(null);
        });

        // Disconnect the player to apply the changes
        player.connection.disconnect(new TranslatableComponent("message.cucubanymod.identity.reset"));
    }
}
