package fr.cucubany.cucubanymod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.IdentityUpdatePacket;
import fr.cucubany.cucubanymod.network.OpenSkillScreenPacket;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

public class SkillCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        /*
         * /skill get <player>
         * /skill <skill> up  <player>
         * /skill <skill> set <player> level      <level>
         * /skill <skill> set <player> aspiration <aspiration>
         */
        dispatcher.register(Commands.literal("skill")
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(SkillCommand::getSkills)))
                .then(Commands.argument("skill", SkillArgument.skill())
                        .then(Commands.literal("up")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(SkillCommand::upSkillLevel)))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.literal("level")
                                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 100))
                                                        .executes(SkillCommand::setSkillLevel)))
                                        .then(Commands.literal("aspiration")
                                                .then(Commands.argument("aspiration", IntegerArgumentType.integer(0))
                                                        .executes(SkillCommand::setSkillAspiration)))))));
    }

    private static int getSkills(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer source = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        Identity targetIdentity = IdentityProvider.getIdentity(target);
        if (targetIdentity == null) {
            return 0;
        }

        IdentityUpdatePacket packet = new IdentityUpdatePacket(target.getUUID(), targetIdentity);
        CucubanyPacketHandler.INSTANCE.sendTo(packet, source.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);

        OpenSkillScreenPacket openSkillScreenPacket = new OpenSkillScreenPacket(target.getUUID());
        CucubanyPacketHandler.INSTANCE.sendTo(openSkillScreenPacket, source.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);

        return 1;
    }

    private static int upSkillLevel(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    private static int setSkillLevel(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    private static int setSkillAspiration(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    private ServerPlayer getPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return EntityArgument.getPlayer(context, "player");
    }
}
