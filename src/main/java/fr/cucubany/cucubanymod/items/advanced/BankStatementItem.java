package fr.cucubany.cucubanymod.items.advanced;

import fr.cucubany.cucubanymod.bank.BankAccount;
import fr.cucubany.cucubanymod.bank.BankTransaction;
import fr.cucubany.cucubanymod.config.CucubanyCommonConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankStatementItem extends Item {

    private static final String TAG_OWNER_NAME  = "ownerName";
    private static final String TAG_OWNER_UUID  = "ownerUUID";
    private static final String TAG_BALANCE     = "balance";
    private static final String TAG_GENERATED   = "generatedAt";
    private static final String TAG_TRANSACTIONS = "transactions";

    public BankStatementItem(Properties properties) {
        super(properties);
    }

    // ── Création d'un relevé ──────────────────────────────────────────────────

    public static ItemStack create(UUID ownerUUID, String ownerName, BankAccount account, long fromDate) {
        ItemStack stack = new ItemStack(fr.cucubany.cucubanymod.items.CucubanyItems.BANK_STATEMENT.get());
        CompoundTag tag = new CompoundTag();
        tag.putUUID(TAG_OWNER_UUID, ownerUUID);
        tag.putString(TAG_OWNER_NAME, ownerName);
        tag.putLong(TAG_BALANCE, account.getBalance());
        tag.putLong(TAG_GENERATED, System.currentTimeMillis());

        ListTag txList = new ListTag();
        for (BankTransaction tx : account.getHistory()) {
            if (fromDate == 0 || tx.timestamp() >= fromDate) txList.add(tx.toNBT());
        }
        tag.put(TAG_TRANSACTIONS, txList);

        stack.setTag(tag);
        return stack;
    }

    // ── Lecture des données ───────────────────────────────────────────────────

    public static String getOwnerName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getString(TAG_OWNER_NAME) : "?";
    }

    public static long getBalance(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getLong(TAG_BALANCE) : 0;
    }

    public static long getGeneratedAt(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getLong(TAG_GENERATED) : 0;
    }

    public static List<BankTransaction> getTransactions(ItemStack stack) {
        List<BankTransaction> list = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag == null) return list;
        ListTag txList = tag.getList(TAG_TRANSACTIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < txList.size(); i++) list.add(BankTransaction.fromNBT(txList.getCompound(i)));
        return list;
    }

    public static boolean hasData(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG_OWNER_NAME);
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!hasData(stack)) return;
        String date = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .format(LocalDateTime.ofInstant(Instant.ofEpochMilli(getGeneratedAt(stack)), ZoneId.systemDefault()));
        tooltip.add(new TranslatableComponent("screen.cucubanymod.atm.statement.tooltip.owner",
                new TextComponent(getOwnerName(stack)).withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("screen.cucubanymod.atm.statement.tooltip.date",
                new TextComponent(date).withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("screen.cucubanymod.atm.statement.tooltip.balance",
                new TextComponent(formatAmount(getBalance(stack))).withStyle(ChatFormatting.GREEN))
                .withStyle(ChatFormatting.GRAY));
    }

    public static String formatAmount(long amount) {
        String raw = String.valueOf(Math.abs(amount));
        StringBuilder sb = new StringBuilder();
        int offset = raw.length() % 3;
        for (int i = 0; i < raw.length(); i++) {
            if (i > 0 && (i - offset) % 3 == 0) sb.append(' ');
            sb.append(raw.charAt(i));
        }
        if (amount < 0) sb.insert(0, '-');
        return sb + " " + CucubanyCommonConfigs.BANK_CURRENCY.get();
    }
}
