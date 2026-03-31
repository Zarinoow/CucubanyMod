package fr.cucubany.cucubanymod.bank;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Dénominations de pièces du mod Coinsje, du plus bas au plus élevé. */
public enum CoinValue {

    COPPER_COIN      ("coinsje:copper_coin",       1,      "Pièce Cuivre"),
    COPPER_COIN_PILE ("coinsje:copper_coin_pile",  9,      "Pile Cuivre"),
    IRON_COIN        ("coinsje:iron_coin",         10,     "Pièce Fer"),
    IRON_COIN_PILE   ("coinsje:iron_coin_pile",    90,     "Pile Fer"),
    GOLD_COIN        ("coinsje:gold_coin",         100,    "Pièce Or"),
    GOLD_COIN_PILE   ("coinsje:gold_coin_pile",    900,    "Pile Or"),
    DIAMOND_COIN     ("coinsje:diamond_coin",      1000,   "Pièce Diamant"),
    DIAMOND_COIN_PILE("coinsje:diamond_coin_pile", 9000,   "Pile Diamant"),
    NETHERITE_COIN   ("coinsje:netherite_coin",    10000,  "Pièce Nether"),
    NETHERITE_COIN_PILE("coinsje:netherite_coin_pile", 90000, "Pile Nether");

    private final String id;
    private final long   value;
    private final String displayName;

    CoinValue(String id, long value, String displayName) {
        this.id          = id;
        this.value       = value;
        this.displayName = displayName;
    }

    public long   getValue()       { return value; }
    public String getDisplayName() { return displayName; }

    /** Retourne l'item associé, ou null si le mod Coinsje n'est pas chargé. */
    @Nullable
    public Item getItem() {
        ResourceLocation loc = new ResourceLocation(id);
        if (!ForgeRegistries.ITEMS.containsKey(loc)) return null;
        return ForgeRegistries.ITEMS.getValue(loc);
    }

    // ── Records utilitaires ───────────────────────────────────────────────────

    public record CoinStack(CoinValue coin, int count) {}
    public record CoinCount(CoinValue coin, long count) {}

    // ── Algorithme glouton ────────────────────────────────────────────────────

    /**
     * Décompose un montant en piles de pièces (max 9 piles, 64 items/pile).
     * Retourne null si impossible (montant trop grand ou mod absent).
     */
    @Nullable
    public static List<CoinStack> breakdown(long amount) {
        if (amount <= 0) return List.of();
        List<CoinStack> result = new ArrayList<>();
        CoinValue[] vals = values();
        for (int i = vals.length - 1; i >= 0 && amount > 0; i--) {
            CoinValue coin = vals[i];
            if (coin.getItem() == null) continue;
            long needed = amount / coin.value;
            if (needed == 0) continue;
            result.add(new CoinStack(coin, (int) needed));
            amount -= needed * coin.value;
        }
        if (amount > 0) return null; // montant non décomposable (mod absent ?)
        int totalStacks = 0;
        for (CoinStack cs : result) totalStacks += (int) Math.ceil((double) cs.count() / 64);
        return totalStacks <= 9 ? result : null;
    }

    // ── Inventaire ────────────────────────────────────────────────────────────

    /** Valeur totale des pièces dans l'inventaire principal du joueur (NBT ignoré). */
    public static long countInInventory(Player player) {
        long total = 0;
        for (var stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            for (CoinValue coin : values()) {
                Item item = coin.getItem();
                if (item != null && stack.getItem() == item) {
                    total += (long) stack.getCount() * coin.value;
                    break;
                }
            }
        }
        return total;
    }

    /** Liste des pièces présentes dans l'inventaire (dénominations non-nulles uniquement). */
    public static List<CoinCount> listInInventory(Player player) {
        long[] counts = new long[values().length];
        for (var stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            for (CoinValue coin : values()) {
                Item item = coin.getItem();
                if (item != null && stack.getItem() == item) {
                    counts[coin.ordinal()] += stack.getCount();
                    break;
                }
            }
        }
        List<CoinCount> result = new ArrayList<>();
        for (CoinValue coin : values()) {
            if (counts[coin.ordinal()] > 0)
                result.add(new CoinCount(coin, counts[coin.ordinal()]));
        }
        return result;
    }

    /**
     * Retire toutes les pièces de l'inventaire du joueur et retourne la valeur totale.
     * À appeler côté SERVEUR uniquement.
     */
    public static long removeAllCoinsFromInventory(Player player) {
        long total = 0;
        for (var stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            for (CoinValue coin : values()) {
                Item item = coin.getItem();
                if (item != null && stack.getItem() == item) {
                    total += (long) stack.getCount() * coin.value;
                    stack.setCount(0);
                    break;
                }
            }
        }
        return total;
    }
}
