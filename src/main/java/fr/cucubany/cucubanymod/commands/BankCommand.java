package fr.cucubany.cucubanymod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.cucubany.cucubanymod.bank.BankAccount;
import fr.cucubany.cucubanymod.bank.BankManager;
import fr.cucubany.cucubanymod.bank.BankTransaction;
import fr.cucubany.cucubanymod.items.advanced.BankStatementItem;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class BankCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bank")
            .requires(src -> src.hasPermission(2))

            // /bank set <player> <amount>
            .then(Commands.literal("set")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(0))
                        .executes(BankCommand::setBalance))))

            // /bank add <player> <amount>
            .then(Commands.literal("add")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(BankCommand::addBalance))))

            // /bank remove <player> <amount>
            .then(Commands.literal("remove")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(BankCommand::removeBalance))))

            // /bank info <player>
            .then(Commands.literal("info")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(BankCommand::info)))

            // /bank resetpin <player>
            .then(Commands.literal("resetpin")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(BankCommand::resetPin)))
        );
    }

    // ── /bank set ──────────────────────────────────────────────────────────────

    private static int setBalance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        long amount = LongArgumentType.getLong(ctx, "amount");
        BankManager bank = BankManager.get(ctx.getSource().getServer());
        BankAccount account = bank.getOrCreate(target.getUUID());
        long old = account.getBalance();
        account.setBalance(amount);
        account.addTransaction(new BankTransaction(BankTransaction.Type.ADMIN_SET, amount,
                System.currentTimeMillis(), "Admin", null));
        bank.setDirty();
        ctx.getSource().sendSuccess(new TextComponent(
            "§a[Banque] Solde de §e" + getPlayerName(target) + " §adéfini à §e" +
            BankStatementItem.formatAmount(amount) + " §a(était §e" + BankStatementItem.formatAmount(old) + "§a)."
        ), true);
        return 1;
    }

    // ── /bank add ──────────────────────────────────────────────────────────────

    private static int addBalance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        long amount = LongArgumentType.getLong(ctx, "amount");
        BankManager bank = BankManager.get(ctx.getSource().getServer());
        BankAccount account = bank.getOrCreate(target.getUUID());
        account.addBalance(amount);
        account.addTransaction(new BankTransaction(BankTransaction.Type.ADMIN_ADD, amount,
                System.currentTimeMillis(), "Admin", null));
        bank.setDirty();
        ctx.getSource().sendSuccess(new TextComponent(
            "§a[Banque] §e+" + BankStatementItem.formatAmount(amount) + " §aajouté au compte de §e" +
            getPlayerName(target) + "§a. Nouveau solde : §e" + BankStatementItem.formatAmount(account.getBalance()) + "§a."
        ), true);
        return 1;
    }

    // ── /bank remove ───────────────────────────────────────────────────────────

    private static int removeBalance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        long amount = LongArgumentType.getLong(ctx, "amount");
        BankManager bank = BankManager.get(ctx.getSource().getServer());
        BankAccount account = bank.getOrCreate(target.getUUID());
        boolean ok = account.removeBalance(amount);
        if (!ok) {
            ctx.getSource().sendFailure(new TextComponent(
                "§c[Banque] Solde insuffisant sur le compte de " + getPlayerName(target) +
                " (" + BankStatementItem.formatAmount(account.getBalance()) + ")."
            ));
            return 0;
        }
        account.addTransaction(new BankTransaction(BankTransaction.Type.ADMIN_REMOVE, amount,
                System.currentTimeMillis(), "Admin", null));
        bank.setDirty();
        ctx.getSource().sendSuccess(new TextComponent(
            "§a[Banque] §e-" + BankStatementItem.formatAmount(amount) + " §adébité du compte de §e" +
            getPlayerName(target) + "§a. Nouveau solde : §e" + BankStatementItem.formatAmount(account.getBalance()) + "§a."
        ), true);
        return 1;
    }

    // ── /bank info ─────────────────────────────────────────────────────────────

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        BankManager bank = BankManager.get(ctx.getSource().getServer());
        BankAccount account = bank.getIfPresent(target.getUUID());

        if (account == null) {
            ctx.getSource().sendSuccess(new TextComponent("§e[Banque] §f" + getPlayerName(target) + " §en'a pas encore de compte."), false);
            return 0;
        }

        ctx.getSource().sendSuccess(new TextComponent("§6════ Compte de §f" + getPlayerName(target) + " §6════"), false);
        ctx.getSource().sendSuccess(new TextComponent("§eSolde : §f" + BankStatementItem.formatAmount(account.getBalance())), false);
        ctx.getSource().sendSuccess(new TextComponent("§ePIN défini : §f" + (account.isPinSet() ? "Oui" : "Non")), false);

        List<BankTransaction> history = account.getHistory();
        if (history.isEmpty()) {
            ctx.getSource().sendSuccess(new TextComponent("§7Aucune transaction."), false);
        } else {
            ctx.getSource().sendSuccess(new TextComponent("§6── Dernières transactions ──"), false);
            int max = Math.min(5, history.size());
            for (int i = 0; i < max; i++) {
                BankTransaction tx = history.get(i);
                String sign = tx.type().isCredit() ? "§a+" : "§c-";
                String name = tx.counterpartName() != null ? " §7(" + tx.counterpartName() + ")" : "";
                ctx.getSource().sendSuccess(new TextComponent(
                    "§7" + tx.getFormattedDate() + " §f" + tx.type().getLabel() +
                    " " + sign + BankStatementItem.formatAmount(tx.amount()) + name
                ), false);
            }
        }
        return 1;
    }

    // ── /bank resetpin ─────────────────────────────────────────────────────────

    private static int resetPin(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        BankManager bank = BankManager.get(ctx.getSource().getServer());
        BankAccount account = bank.getOrCreate(target.getUUID());
        account.resetPin();
        bank.setDirty();
        ctx.getSource().sendSuccess(new TextComponent(
            "§a[Banque] PIN de §e" + getPlayerName(target) + " §aréinitialisé."
        ), true);
        return 1;
    }

    // ── Util ───────────────────────────────────────────────────────────────────

    private static String getPlayerName(ServerPlayer player) {
        Identity id = IdentityProvider.getIdentity(player);
        return id != null ? id.getFullName() : player.getName().getString();
    }
}
