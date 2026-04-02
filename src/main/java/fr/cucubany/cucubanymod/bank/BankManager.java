package fr.cucubany.cucubanymod.bank;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankManager extends SavedData {

    private static final String DATA_NAME = "cucubanymod_bank";

    private final Map<UUID, BankAccount> accounts = new HashMap<>();

    private BankManager() {}

    // ── Accès global ─────────────────────────────────────────────────────────

    public static BankManager get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(BankManager::load, BankManager::new, DATA_NAME);
    }

    // ── CRUD comptes ─────────────────────────────────────────────────────────

    /** Récupère ou crée un compte. */
    public BankAccount getOrCreate(UUID uuid) {
        return accounts.computeIfAbsent(uuid, k -> new BankAccount());
    }

    /** Retourne null si le compte n'existe pas encore. */
    public BankAccount getIfPresent(UUID uuid) {
        return accounts.get(uuid);
    }

    /** Supprime le compte bancaire d'un joueur (reset CK). */
    public void resetAccount(UUID uuid) {
        accounts.remove(uuid);
        setDirty();
    }

    // ── Virement ─────────────────────────────────────────────────────────────

    /**
     * Vire {@code amount} de {@code from} vers {@code to}.
     * Ne vérifie PAS le PIN — à faire avant d'appeler.
     * @return false si solde insuffisant
     */
    public boolean transfer(UUID from, UUID to, long amount, String fromName, String toName) {
        BankAccount fromAcc = getOrCreate(from);
        if (!fromAcc.removeBalance(amount)) return false;
        BankAccount toAcc = getOrCreate(to);
        toAcc.addBalance(amount);
        long ts = System.currentTimeMillis();
        fromAcc.addTransaction(new BankTransaction(BankTransaction.Type.TRANSFER_OUT, amount, ts, toName, to));
        toAcc.addTransaction(new BankTransaction(BankTransaction.Type.TRANSFER_IN, amount, ts, fromName, from));
        setDirty();
        return true;
    }

    // ── NBT (SavedData) ───────────────────────────────────────────────────────

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag accs = new CompoundTag();
        accounts.forEach((uuid, acc) -> accs.put(uuid.toString(), acc.toNBT()));
        tag.put("accounts", accs);
        return tag;
    }

    private static BankManager load(CompoundTag tag) {
        BankManager mgr = new BankManager();
        CompoundTag accs = tag.getCompound("accounts");
        for (String key : accs.getAllKeys()) {
            try { mgr.accounts.put(UUID.fromString(key), BankAccount.fromNBT(accs.getCompound(key))); }
            catch (IllegalArgumentException ignored) {}
        }
        return mgr;
    }
}
