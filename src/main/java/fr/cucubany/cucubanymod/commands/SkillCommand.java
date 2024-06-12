package fr.cucubany.cucubanymod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class SkillCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        /*
         * /skill <skill> get <player>
         * /skill <skill> up  <player>
         * /skill <skill> set <player> level      <level>
         * /skill <skill> set <player> aspiration <aspiration>
         */
        dispatcher.register(Commands.literal("skill")
                .then(Commands.argument("skill", SkillArgument.skill())
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(SkillCommand::getSkillLevel)))
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

    private static int getSkillLevel(CommandContext<CommandSourceStack> context) {

        return 0;
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
