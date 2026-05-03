package fr.cucubany.cucubanymod.bank;

import fr.cucubany.cucubanymod.wallet.WalletCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Dénominations de pièces du mod Coinsje, du plus bas au plus élevé. */
public enum CoinValue {

    COPPER_COIN      ("coinsje:copper_coin",       1,      "Pièce Cuivre",   "bank.cucubanymod.coin.copper_coin"),
    COPPER_COIN_PILE ("coinsje:copper_coin_pile",  9,      "Pile Cuivre",    "bank.cucubanymod.coin.copper_pile"),
    IRON_COIN        ("coinsje:iron_coin",         10,     "Pièce Fer",      "bank.cucubanymod.coin.iron_coin"),
    IRON_COIN_PILE   ("coinsje:iron_coin_pile",    90,     "Pile Fer",       "bank.cucubanymod.coin.iron_pile"),
    GOLD_COIN        ("coinsje:gold_coin",         100,    "Pièce Or",       "bank.cucubanymod.coin.gold_coin"),
    GOLD_COIN_PILE   ("coinsje:gold_coin_pile",    900,    "Pile Or",        "bank.cucubanymod.coin.gold_pile"),
    DIAMOND_COIN     ("coinsje:diamond_coin",      1000,   "Pièce Diamant",  "bank.cucubanymod.coin.diamond_coin"),
    DIAMOND_COIN_PILE("coinsje:diamond_coin_pile", 9000,   "Pile Diamant",   "bank.cucubanymod.coin.diamond_pile"),
    NETHERITE_COIN   ("coinsje:netherite_coin",    10000,  "Pièce Nether",   "bank.cucubanymod.coin.netherite_coin"),
    NETHERITE_COIN_PILE("coinsje:netherite_coin_pile", 90000, "Pile Nether", "bank.cucubanymod.coin.netherite_pile");

    private final String id;
    private final long   value;
    private final String displayName;
    private final String langKey;

    CoinValue(String id, long value, String displayName, String langKey) {
        this.id          = id;
        this.value       = value;
        this.displayName = displayName;
        this.langKey     = langKey;
    }

    public long   getValue()       { return value; }
    public String getDisplayName() { return displayName; }
    public String getLangKey()     { return langKey; }

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

    /** Valeur totale des pièces dans l'inventaire principal et le wallet du joueur. */
    public static long countInInventory(Player player) {
        long total = 0;
        for (var stack : player.getInventory().items) {
            CoinValue cv = fromItem(stack.getItem());
            if (cv != null) total += (long) stack.getCount() * cv.value;
        }
        var walletOpt = player.getCapability(WalletCapabilityProvider.WALLET_CAPABILITY).resolve();
        if (walletOpt.isPresent()) {
            var container = walletOpt.get().getContainer();
            for (int i = 1; i <= 10; i++) {
                ItemStack stack = container.getItem(i);
                CoinValue cv = fromItem(stack.getItem());
                if (cv != null) total += (long) stack.getCount() * cv.value;
            }
        }
        return total;
    }

    /** Liste des pièces dans l'inventaire et le wallet (dénominations non-nulles uniquement). */
    public static List<CoinCount> listInInventory(Player player) {
        long[] counts = new long[values().length];
        for (var stack : player.getInventory().items) {
            CoinValue cv = fromItem(stack.getItem());
            if (cv != null) counts[cv.ordinal()] += stack.getCount();
        }
        var walletOpt = player.getCapability(WalletCapabilityProvider.WALLET_CAPABILITY).resolve();
        if (walletOpt.isPresent()) {
            var container = walletOpt.get().getContainer();
            for (int i = 1; i <= 10; i++) {
                ItemStack stack = container.getItem(i);
                CoinValue cv = fromItem(stack.getItem());
                if (cv != null) counts[cv.ordinal()] += stack.getCount();
            }
        }
        List<CoinCount> result = new ArrayList<>();
        for (CoinValue coin : values()) {
            if (counts[coin.ordinal()] > 0)
                result.add(new CoinCount(coin, counts[coin.ordinal()]));
        }
        return result;
    }

    // ── Helpers dénomination ─────────────────────────────────────────────────

    /** Vrai si cette dénomination est une pile (copper_coin_pile, etc.). */
    public boolean isPile() {
        return ordinal() % 2 == 1; // piles aux indices impairs selon l'ordre de l'enum
    }

    /**
     * Retourne la dénomination appairée : pile ↔ pièce simple.
     * Ex : COPPER_COIN → COPPER_COIN_PILE, et inversement.
     */
    @Nullable
    public CoinValue getPairedDenomination() {
        CoinValue[] vals = values();
        int idx = ordinal();
        return isPile() ? (idx > 0 ? vals[idx - 1] : null)
                        : (idx + 1 < vals.length ? vals[idx + 1] : null);
    }

    /**
     * Retourne le CoinValue correspondant à l'item donné, ou null si inconnu.
     */
    @Nullable
    public static CoinValue fromItem(net.minecraft.world.item.Item item) {
        if (item == null) return null;
        for (CoinValue cv : values()) {
            Item cvItem = cv.getItem();
            if (cvItem != null && cvItem == item) return cv;
        }
        return null;
    }

    /**
     * Retire toutes les pièces de l'inventaire et du wallet du joueur, retourne la valeur totale.
     * À appeler côté SERVEUR uniquement.
     */
    public static long removeAllCoinsFromInventory(Player player) {
        long total = 0;
        for (var stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            CoinValue cv = fromItem(stack.getItem());
            if (cv != null) {
                total += (long) stack.getCount() * cv.value;
                stack.setCount(0);
            }
        }
        var walletOpt = player.getCapability(WalletCapabilityProvider.WALLET_CAPABILITY).resolve();
        if (walletOpt.isPresent()) {
            var container = walletOpt.get().getContainer();
            for (int i = 1; i <= 10; i++) {
                ItemStack stack = container.getItem(i);
                if (stack.isEmpty()) continue;
                CoinValue cv = fromItem(stack.getItem());
                if (cv != null) {
                    total += (long) stack.getCount() * cv.value;
                    container.setItem(i, ItemStack.EMPTY);
                }
            }
        }
        return total;
    }

    /**
     * Essaie d'ajouter {@code count} pièces/piles du type {@code coinType} dans le wallet.
     * Respecte le verrouillage par dénomination des paires CoinSlot/PileSlot.
     * Retourne le nombre qui n'a pas pu être ajouté (overflow à placer en inventaire).
     * À appeler côté SERVEUR uniquement.
     */
    public static int addCoinsToWallet(Player player, CoinValue coinType, int count) {
        var walletOpt = player.getCapability(WalletCapabilityProvider.WALLET_CAPABILITY).resolve();
        if (walletOpt.isEmpty()) return count;
        var container = walletOpt.get().getContainer();

        Item coinItem = coinType.getItem();
        if (coinItem == null) return count;

        // Slots 1-5 : CoinSlots ; slots 6-10 : PileSlots
        boolean isPile  = coinType.isPile();
        int startSlot   = isPile ? 6 : 1;
        int endSlot     = isPile ? 11 : 6;
        int remaining   = count;

        // Passe 1 : compléter les stacks existants du même type
        for (int i = startSlot; i < endSlot && remaining > 0; i++) {
            ItemStack existing = container.getItem(i);
            if (!existing.isEmpty() && existing.getItem() == coinItem) {
                int adding = Math.min(64 - existing.getCount(), remaining);
                if (adding > 0) {
                    container.setItem(i, new ItemStack(coinItem, existing.getCount() + adding));
                    remaining -= adding;
                }
            }
        }

        // Passe 2 : slots vides en respectant le verrouillage du slot apparié
        for (int i = startSlot; i < endSlot && remaining > 0; i++) {
            if (!container.getItem(i).isEmpty()) continue;

            if (isPile) {
                // PileSlot i (6-10) apparié avec CoinSlot (i-5)
                ItemStack paired = container.getItem(i - 5);
                if (!paired.isEmpty()) {
                    CoinValue pairedCv = fromItem(paired.getItem());
                    if (pairedCv != null && pairedCv.getPairedDenomination() != coinType) continue;
                }
            } else {
                // CoinSlot i (1-5) apparié avec PileSlot (i+5)
                ItemStack paired = container.getItem(i + 5);
                if (!paired.isEmpty()) {
                    CoinValue pairedCv = fromItem(paired.getItem());
                    if (pairedCv != null && pairedCv.getPairedDenomination() != coinType) continue;
                }
            }

            int adding = Math.min(64, remaining);
            container.setItem(i, new ItemStack(coinItem, adding));
            remaining -= adding;
        }

        autoConvertWalletContainer(container);
        return remaining;
    }

    /**
     * Convertit les pièces en piles dans le container du wallet (même logique que
     * autoConvertCoins du mixin, mais sur le SimpleContainer brut).
     * Indices : CoinSlots 1-5, PileSlots 6-10, pairing ci → ci+5.
     */
    private static void autoConvertWalletContainer(SimpleContainer container) {
        for (int ci = 1; ci <= 5; ci++) {
            ItemStack coinStack = container.getItem(ci);
            if (coinStack.isEmpty() || coinStack.getCount() < 9) continue;

            CoinValue coinValue = fromItem(coinStack.getItem());
            if (coinValue == null || coinValue.isPile()) continue;

            CoinValue pileValue = coinValue.getPairedDenomination();
            if (pileValue == null) continue;
            Item pileItem = pileValue.getItem();
            if (pileItem == null) continue;

            int pi = ci + 5;
            ItemStack pileStack = container.getItem(pi);
            int currentPileCount = 0;
            if (!pileStack.isEmpty()) {
                if (pileStack.getItem() != pileItem) continue; // dénomination différente
                currentPileCount = pileStack.getCount();
            }

            int count          = coinStack.getCount();
            int pilesToCreate  = count / 9;
            int remainingCoins = count % 9;
            int pilesConverted = Math.min(pilesToCreate, 64 - currentPileCount);
            if (pilesConverted == 0) continue;

            int newCoinCount = remainingCoins + (pilesToCreate - pilesConverted) * 9;
            Item coinItem = coinValue.getItem();
            container.setItem(ci, newCoinCount == 0 || coinItem == null
                    ? ItemStack.EMPTY : new ItemStack(coinItem, newCoinCount));
            container.setItem(pi, new ItemStack(pileItem, currentPileCount + pilesConverted));
        }
    }
}
