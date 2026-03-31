package fr.cucubany.cucubanymod.bank;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class BankAccount {

    public static final int MAX_HISTORY = 20;

    private long balance;
    private int pin;
    private boolean pinSet;
    private final Deque<BankTransaction> history = new ArrayDeque<>();

    public BankAccount() {}

    // ── Getters ──────────────────────────────────────────────────────────────

    public long getBalance() { return balance; }
    public boolean isPinSet() { return pinSet; }
    public List<BankTransaction> getHistory() { return new ArrayList<>(history); }

    // ── PIN ──────────────────────────────────────────────────────────────────

    public void setPin(int pin) { this.pin = pin; this.pinSet = true; }
    public void resetPin() { this.pin = 0; this.pinSet = false; }
    public boolean verifyPin(int input) { return pinSet && this.pin == input; }

    // ── Balance ───────────────────────────────────────────────────────────────

    public void setBalance(long amount) { this.balance = Math.max(0, amount); }
    public void addBalance(long amount) { this.balance += amount; }

    /** @return false si solde insuffisant */
    public boolean removeBalance(long amount) {
        if (balance < amount) return false;
        this.balance -= amount;
        return true;
    }

    // ── Historique ────────────────────────────────────────────────────────────

    public void addTransaction(BankTransaction tx) {
        history.addFirst(tx);
        while (history.size() > MAX_HISTORY) history.removeLast();
    }

    // ── NBT ──────────────────────────────────────────────────────────────────

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("balance", balance);
        tag.putBoolean("pinSet", pinSet);
        if (pinSet) tag.putInt("pin", pin);
        ListTag hist = new ListTag();
        for (BankTransaction tx : history) hist.add(tx.toNBT());
        tag.put("history", hist);
        return tag;
    }

    public static BankAccount fromNBT(CompoundTag tag) {
        BankAccount acc = new BankAccount();
        acc.balance = tag.getLong("balance");
        acc.pinSet = tag.getBoolean("pinSet");
        if (acc.pinSet) acc.pin = tag.getInt("pin");
        ListTag hist = tag.getList("history", Tag.TAG_COMPOUND);
        for (int i = 0; i < hist.size(); i++) acc.history.addLast(BankTransaction.fromNBT(hist.getCompound(i)));
        return acc;
    }
}
