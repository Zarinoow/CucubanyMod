package fr.cucubany.cucubanymod.bank;

import net.minecraft.nbt.CompoundTag;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record BankTransaction(Type type, long amount, long timestamp, String counterpartName, UUID counterpartUUID) {

    public enum Type {
        TRANSFER_IN("bank.cucubanymod.tx.transfer_in", true),
        TRANSFER_OUT("bank.cucubanymod.tx.transfer_out", false),
        ADMIN_ADD("bank.cucubanymod.tx.admin_add", true),
        ADMIN_REMOVE("bank.cucubanymod.tx.admin_remove", false),
        ADMIN_SET("bank.cucubanymod.tx.admin_set", true),
        DEPOSIT("bank.cucubanymod.tx.deposit", true),
        WITHDRAWAL("bank.cucubanymod.tx.withdrawal", false);

        private final String label;
        private final boolean credit;

        Type(String label, boolean credit) {
            this.label = label;
            this.credit = credit;
        }

        public String getLabel() { return label; }
        public boolean isCredit() { return credit; }
    }

    public String getFormattedDate() {
        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern("dd/MM HH:mm").format(dt);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.name());
        tag.putLong("amount", amount);
        tag.putLong("ts", timestamp);
        tag.putString("name", counterpartName != null ? counterpartName : "");
        if (counterpartUUID != null) tag.putUUID("uuid", counterpartUUID);
        return tag;
    }

    public static BankTransaction fromNBT(CompoundTag tag) {
        Type type = Type.valueOf(tag.getString("type"));
        long amount = tag.getLong("amount");
        long ts = tag.getLong("ts");
        String name = tag.getString("name");
        UUID uuid = tag.hasUUID("uuid") ? tag.getUUID("uuid") : null;
        return new BankTransaction(type, amount, ts, name.isEmpty() ? null : name, uuid);
    }
}
