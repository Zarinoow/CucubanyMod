package fr.cucubany.cucubanymod.network.bank;

import fr.cucubany.cucubanymod.bank.BankTransaction;
import fr.cucubany.cucubanymod.client.screen.ATMScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Paquet Serveur → Client : synchronisation données bancaires. */
public class BankSyncPacket {

    public enum Status {
        OK, ERROR, PIN_OK, PIN_INVALID, INSUFFICIENT_FUNDS, TRANSFER_OK, STATEMENT_OK, WITHDRAW_OK, DEPOSIT_OK, BALANCE_UPDATE
    }

    public record PlayerEntry(UUID uuid, String name) {}

    // ── Données ───────────────────────────────────────────────────────────────

    private final Status status;
    private final long balance;
    private final boolean hasPin;
    private final List<BankTransaction> transactions;
    private final List<PlayerEntry> onlinePlayers;
    private final String message;

    public BankSyncPacket(Status status, long balance, boolean hasPin,
                          List<BankTransaction> transactions, List<PlayerEntry> onlinePlayers, String message) {
        this.status = status;
        this.balance = balance;
        this.hasPin = hasPin;
        this.transactions = transactions;
        this.onlinePlayers = onlinePlayers;
        this.message = message;
    }

    public Status getStatus()                    { return status; }
    public long getBalance()                     { return balance; }
    public boolean isHasPin()                    { return hasPin; }
    public List<BankTransaction> getTransactions() { return transactions; }
    public List<PlayerEntry> getOnlinePlayers()  { return onlinePlayers; }
    public String getMessage()                   { return message; }

    // ── Sérialisation ─────────────────────────────────────────────────────────

    public static void encode(BankSyncPacket p, FriendlyByteBuf buf) {
        buf.writeByte(p.status.ordinal());
        buf.writeLong(p.balance);
        buf.writeBoolean(p.hasPin);
        buf.writeUtf(p.message);

        buf.writeInt(p.transactions.size());
        for (BankTransaction tx : p.transactions) buf.writeNbt(tx.toNBT());

        buf.writeInt(p.onlinePlayers.size());
        for (PlayerEntry e : p.onlinePlayers) {
            buf.writeUUID(e.uuid());
            buf.writeUtf(e.name());
        }
    }

    public static BankSyncPacket decode(FriendlyByteBuf buf) {
        Status status = Status.values()[buf.readByte()];
        long balance = buf.readLong();
        boolean hasPin = buf.readBoolean();
        String message = buf.readUtf();

        int txCount = buf.readInt();
        List<BankTransaction> txList = new ArrayList<>(txCount);
        for (int i = 0; i < txCount; i++) txList.add(BankTransaction.fromNBT(buf.readNbt()));

        int playerCount = buf.readInt();
        List<PlayerEntry> players = new ArrayList<>(playerCount);
        for (int i = 0; i < playerCount; i++) players.add(new PlayerEntry(buf.readUUID(), buf.readUtf()));

        return new BankSyncPacket(status, balance, hasPin, txList, players, message);
    }

    // ── Handler (côté CLIENT) ─────────────────────────────────────────────────

    public static void handle(BankSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof ATMScreen atm) {
                atm.handleServerResponse(packet);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
