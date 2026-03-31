package fr.cucubany.cucubanymod.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.cucubany.cucubanymod.bank.BankTransaction;
import fr.cucubany.cucubanymod.config.CucubanyCommonConfigs;
import fr.cucubany.cucubanymod.items.CucubanyItems;
import fr.cucubany.cucubanymod.items.advanced.BankStatementItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.IIngameOverlay;

import java.util.List;

public class BankStatementOverlay {

    // Dimensions du document affiché
    private static final int DOC_W = 220;
    private static final int HEADER_H = 50;
    private static final int ROW_H = 10;
    private static final int PADDING = 8;

    // Couleurs papier
    private static final int COL_BG      = 0xF0F5F0DC; // crème
    private static final int COL_HEADER  = 0xFFD4C9A0; // beige foncé
    private static final int COL_BORDER  = 0xFF8A7A50;
    private static final int COL_TEXT    = 0xFF2A2010;
    private static final int COL_CREDIT  = 0xFF1A6020;
    private static final int COL_DEBIT   = 0xFF8A1010;
    private static final int COL_TITLE   = 0xFF5A3A00;

    public static final IIngameOverlay HUD_OVERLAY = (gui, poseStack, partialTicks, width, height) -> {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) return;

        // Vérifie si le joueur tient un relevé en main principale ou secondaire
        ItemStack held = player.getMainHandItem();
        if (!isStatement(held)) {
            held = player.getOffhandItem();
            if (!isStatement(held)) return;
        }

        if (!BankStatementItem.hasData(held)) return;

        String ownerName  = BankStatementItem.getOwnerName(held);
        long balance       = BankStatementItem.getBalance(held);
        List<BankTransaction> txList = BankStatementItem.getTransactions(held);

        int docH = HEADER_H + PADDING + txList.size() * ROW_H + PADDING + 4;
        int docX = (width - DOC_W) / 2;
        int docY = (height - docH) / 2;

        RenderSystem.enableBlend();
        poseStack.pushPose();

        // ── Fond ──────────────────────────────────────────────────────────────
        drawRect(poseStack, docX, docY, DOC_W, docH, COL_BG);
        // Bordure
        drawBorder(poseStack, docX, docY, DOC_W, docH, COL_BORDER);

        // ── En-tête ───────────────────────────────────────────────────────────
        drawRect(poseStack, docX, docY, DOC_W, HEADER_H, COL_HEADER);
        drawBorder(poseStack, docX, docY, DOC_W, HEADER_H, COL_BORDER);

        int cx = docX + DOC_W / 2;
        drawCentered(poseStack, mc, I18n.get("screen.cucubanymod.atm.statement.title", CucubanyCommonConfigs.BANK_NAME.get()).toUpperCase(), cx, docY + 6, COL_TITLE);
        drawCentered(poseStack, mc, I18n.get("screen.cucubanymod.atm.statement.owner", ownerName), cx, docY + 18, COL_TEXT);
        drawCentered(poseStack, mc, I18n.get("screen.cucubanymod.atm.solde", BankStatementItem.formatAmount(balance)), cx, docY + 30, COL_CREDIT);

        // ── Ligne séparatrice ─────────────────────────────────────────────────
        int lineY = docY + HEADER_H + 2;
        drawRect(poseStack, docX + 4, lineY, DOC_W - 8, 1, COL_BORDER);

        // ── Transactions ──────────────────────────────────────────────────────
        if (txList.isEmpty()) {
            drawCentered(poseStack, mc, I18n.get("screen.cucubanymod.atm.history.empty"), cx, lineY + 6, COL_TEXT);
        } else {
            int rowY = lineY + 4;
            for (BankTransaction tx : txList) {
                String sign = tx.type().isCredit() ? "+" : "-";
                int color = tx.type().isCredit() ? COL_CREDIT : COL_DEBIT;

                // Date + libellé
                String left = tx.getFormattedDate() + "  " + I18n.get(tx.type().getLabel());
                drawText(poseStack, mc, left, docX + PADDING, rowY, COL_TEXT);

                // Montant (droite)
                String right = sign + BankStatementItem.formatAmount(tx.amount());
                int rw = mc.font.width(right);
                drawText(poseStack, mc, right, docX + DOC_W - PADDING - rw, rowY, color);

                rowY += ROW_H;
            }
        }

        poseStack.popPose();
        RenderSystem.disableBlend();
    };

    // ── Helpers de dessin ─────────────────────────────────────────────────────

    private static boolean isStatement(ItemStack stack) {
        return stack.is(CucubanyItems.BANK_STATEMENT.get());
    }

    private static void drawRect(PoseStack ps, int x, int y, int w, int h, int color) {
        GuiComponent.fill(ps, x, y, x + w, y + h, color);
    }

    private static void drawBorder(PoseStack ps, int x, int y, int w, int h, int color) {
        GuiComponent.fill(ps, x, y, x + w, y + 1, color);          // top
        GuiComponent.fill(ps, x, y + h - 1, x + w, y + h, color);  // bottom
        GuiComponent.fill(ps, x, y, x + 1, y + h, color);           // left
        GuiComponent.fill(ps, x + w - 1, y, x + w, y + h, color);   // right
    }

    private static void drawCentered(PoseStack ps, Minecraft mc, String text, int cx, int y, int color) {
        mc.font.draw(ps, text, cx - mc.font.width(text) / 2f, y, color);
    }

    private static void drawText(PoseStack ps, Minecraft mc, String text, int x, int y, int color) {
        mc.font.draw(ps, text, x, y, color);
    }
}
