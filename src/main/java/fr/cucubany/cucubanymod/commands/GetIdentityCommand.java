package fr.cucubany.cucubanymod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.cucubany.cucubanymod.capabilities.IIdentityCapability;
import fr.cucubany.cucubanymod.capabilities.IdentityCapabilityProvider;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Collection;

public class GetIdentityCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal("identity")
                .then(Commands.literal("get")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(GetIdentityCommand::execute)))
                .requires(cs -> cs.hasPermission(2)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");

        if (players.isEmpty()) {
            return 0;
        }

        ServerPlayer sender = context.getSource().getPlayerOrException();

        players.forEach(player -> {
            Identity identity = IdentityProvider.getIdentity(player);
            if (identity != null && !identity.getFirstName().isEmpty() && !identity.getLastName().isEmpty()) {
                sender.sendMessage(new TranslatableComponent("message.cucubanymod.identity.get", player.getName().getString(), identity.getFirstName(), identity.getLastName()), sender.getUUID());
            } else {
                sender.sendMessage(new TranslatableComponent("message.cucubanymod.identity.get.empty", player.getName().getString()), sender.getUUID());
            }
        });
        return 1;
    }
}
