package fr.cucubany.cucubanymod.network.bank;

import fr.cucubany.cucubanymod.bank.BankAccount;
import fr.cucubany.cucubanymod.bank.BankManager;
import fr.cucubany.cucubanymod.bank.BankTransaction;
import fr.cucubany.cucubanymod.bank.CoinValue;
import fr.cucubany.cucubanymod.items.advanced.BankStatementItem;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.roleplay.Identity;
import fr.cucubany.cucubanymod.roleplay.IdentityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Paquet Client → Serveur : action ATM. */
public record BankActionPacket(Action action, int pin, UUID targetUUID, long amount) {

    public enum Action { GET_DATA, SETUP_PIN, VERIFY_PIN, TRANSFER, GET_STATEMENT, DEPOSIT, WITHDRAW }

    // ── Sérialisation ─────────────────────────────────────────────────────────

    public static void encode(BankActionPacket p, FriendlyByteBuf buf) {
        buf.writeByte(p.action.ordinal());
        buf.writeInt(p.pin);
        buf.writeBoolean(p.targetUUID != null);
        if (p.targetUUID != null) buf.writeUUID(p.targetUUID);
        buf.writeLong(p.amount);
    }

    public static BankActionPacket decode(FriendlyByteBuf buf) {
        Action action = Action.values()[buf.readByte()];
        int pin = buf.readInt();
        UUID target = buf.readBoolean() ? buf.readUUID() : null;
        long amount = buf.readLong();
        return new BankActionPacket(action, pin, target, amount);
    }

    // ── Handler (côté SERVEUR) ────────────────────────────────────────────────

    public static void handle(BankActionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;

            BankManager bank = BankManager.get(sender.getServer());
            BankAccount account = bank.getOrCreate(sender.getUUID());

            switch (packet.action()) {

                case GET_DATA -> sendFull(sender, bank, account, BankSyncPacket.Status.OK, "");

                case SETUP_PIN -> {
                    if (account.isPinSet()) { sendError(sender, "PIN déjà défini."); return; }
                    account.setPin(packet.pin());
                    bank.setDirty();
                    sendFull(sender, bank, account, BankSyncPacket.Status.PIN_OK, "PIN défini avec succès.");
                }

                case VERIFY_PIN -> {
                    if (!account.isPinSet()) { sendError(sender, "Aucun compte existant."); return; }
                    if (!account.verifyPin(packet.pin())) {
                        sendStatus(sender, BankSyncPacket.Status.PIN_INVALID, "PIN incorrect.", account);
                        return;
                    }
                    sendFull(sender, bank, account, BankSyncPacket.Status.PIN_OK, "");
                }

                case TRANSFER -> {
                    if (!account.verifyPin(packet.pin())) {
                        sendStatus(sender, BankSyncPacket.Status.PIN_INVALID, "PIN incorrect.", account);
                        return;
                    }
                    UUID targetUUID = packet.targetUUID();
                    long amount = packet.amount();
                    if (targetUUID == null || amount <= 0) { sendError(sender, "Paramètres invalides."); return; }
                    if (targetUUID.equals(sender.getUUID())) { sendError(sender, "Auto-virement impossible."); return; }

                    String fromName = getPlayerName(sender);
                    String toName = resolvePlayerName(sender, targetUUID);

                    boolean ok = bank.transfer(sender.getUUID(), targetUUID, amount, fromName, toName);
                    if (!ok) {
                        sendStatus(sender, BankSyncPacket.Status.INSUFFICIENT_FUNDS, "Solde insuffisant.", account);
                        return;
                    }

                    // Notification au destinataire s'il est connecté
                    ServerPlayer target = sender.getServer().getPlayerList().getPlayer(targetUUID);
                    if (target != null) {
                        target.sendMessage(new TextComponent(
                            "§a[Banque] §eVirement reçu : §f" + BankStatementItem.formatAmount(amount) +
                            " §ede §f" + fromName + "§e."
                        ), new UUID(0, 0));
                    }

                    sendFull(sender, bank, account, BankSyncPacket.Status.TRANSFER_OK,
                        "Virement de " + BankStatementItem.formatAmount(amount) + " à " + toName + " effectué.");
                }

                case GET_STATEMENT -> {
                    if (!account.verifyPin(packet.pin())) {
                        sendStatus(sender, BankSyncPacket.Status.PIN_INVALID, "PIN incorrect.", account);
                        return;
                    }
                    ItemStack statement = BankStatementItem.create(sender.getUUID(), getPlayerName(sender), account);
                    if (!sender.addItem(statement)) sender.drop(statement, false);
                    sendStatus(sender, BankSyncPacket.Status.STATEMENT_OK, "Relevé généré dans votre inventaire.", account);
                }

                case DEPOSIT -> {
                    if (!account.verifyPin(packet.pin())) {
                        sendStatus(sender, BankSyncPacket.Status.PIN_INVALID, "PIN incorrect.", account);
                        return;
                    }
                    long total = CoinValue.removeAllCoinsFromInventory(sender);
                    if (total == 0) {
                        sendError(sender, "Aucune pièce dans votre inventaire.");
                        return;
                    }
                    account.addBalance(total);
                    account.addTransaction(new BankTransaction(
                        BankTransaction.Type.DEPOSIT, total, System.currentTimeMillis(), null, null));
                    bank.setDirty();
                    sendFull(sender, bank, account, BankSyncPacket.Status.DEPOSIT_OK,
                        "Dépôt de " + BankStatementItem.formatAmount(total) + " effectué.");
                }

                case WITHDRAW -> {
                    if (!account.verifyPin(packet.pin())) {
                        sendStatus(sender, BankSyncPacket.Status.PIN_INVALID, "PIN incorrect.", account);
                        return;
                    }
                    long amount = packet.amount();
                    if (amount <= 0) { sendError(sender, "Montant invalide."); return; }
                    var breakdown = CoinValue.breakdown(amount);
                    if (breakdown == null) {
                        sendError(sender, "Montant non distribuable en ≤9 piles.");
                        return;
                    }
                    if (!account.removeBalance(amount)) {
                        sendStatus(sender, BankSyncPacket.Status.INSUFFICIENT_FUNDS, "Solde insuffisant.", account);
                        return;
                    }
                    for (CoinValue.CoinStack cs : breakdown) {
                        Item item = cs.coin().getItem();
                        if (item == null) continue;
                        int remaining = cs.count();
                        while (remaining > 0) {
                            int stackSize = Math.min(remaining, 64);
                            ItemStack stack = new ItemStack(item, stackSize);
                            if (!sender.addItem(stack)) sender.drop(stack, false);
                            remaining -= stackSize;
                        }
                    }
                    account.addTransaction(new BankTransaction(
                        BankTransaction.Type.WITHDRAWAL, amount, System.currentTimeMillis(), null, null));
                    bank.setDirty();
                    sendFull(sender, bank, account, BankSyncPacket.Status.WITHDRAW_OK,
                        "Retrait de " + BankStatementItem.formatAmount(amount) + " effectué.");
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String getPlayerName(ServerPlayer player) {
        Identity id = IdentityProvider.getIdentity(player);
        return id != null ? id.getFullName() : player.getName().getString();
    }

    private static String resolvePlayerName(ServerPlayer sender, UUID targetUUID) {
        ServerPlayer target = sender.getServer().getPlayerList().getPlayer(targetUUID);
        if (target != null) {
            Identity id = IdentityProvider.getIdentity(target);
            return id != null ? id.getFullName() : target.getName().getString();
        }
        return targetUUID.toString().substring(0, 8);
    }

    private static void sendFull(ServerPlayer player, BankManager bank, BankAccount account,
                                  BankSyncPacket.Status status, String msg) {
        List<BankSyncPacket.PlayerEntry> online = player.getServer().getPlayerList().getPlayers().stream()
            .filter(p -> !p.getUUID().equals(player.getUUID()))
            .map(p -> {
                Identity id = IdentityProvider.getIdentity(p);
                String name = id != null ? id.getFullName() : p.getName().getString();
                return new BankSyncPacket.PlayerEntry(p.getUUID(), name);
            })
            .collect(Collectors.toList());

        send(player, new BankSyncPacket(status, account.getBalance(), account.isPinSet(),
                account.getHistory(), online, msg));
    }

    private static void sendStatus(ServerPlayer player, BankSyncPacket.Status status, String msg, BankAccount account) {
        send(player, new BankSyncPacket(status, account.getBalance(), account.isPinSet(),
                List.of(), List.of(), msg));
    }

    private static void sendError(ServerPlayer player, String msg) {
        send(player, new BankSyncPacket(BankSyncPacket.Status.ERROR, 0, false, List.of(), List.of(), msg));
    }

    private static void send(ServerPlayer player, BankSyncPacket packet) {
        CucubanyPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
