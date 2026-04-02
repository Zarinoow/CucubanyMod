package fr.cucubany.cucubanymod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.cucubany.cucubanymod.CucubanyMod;
import fr.cucubany.cucubanymod.bank.BankTransaction;
import fr.cucubany.cucubanymod.bank.CoinValue;
import fr.cucubany.cucubanymod.items.advanced.BankStatementItem;
import fr.cucubany.cucubanymod.network.CucubanyPacketHandler;
import fr.cucubany.cucubanymod.network.bank.ATMSoundPacket;
import fr.cucubany.cucubanymod.network.bank.BankActionPacket;
import fr.cucubany.cucubanymod.network.bank.BankSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import fr.cucubany.cucubanymod.config.CucubanyCommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ATMScreen extends Screen {

    // ── Textures ──────────────────────────────────────────────────────────────
    private static final ResourceLocation atmTexture  = new ResourceLocation(CucubanyMod.MOD_ID, "textures/gui/atm_ui.png");
    private static final ResourceLocation atmElements = new ResourceLocation(CucubanyMod.MOD_ID, "textures/gui/atm_btn.png");

    // ── Boutons ───────────────────────────────────────────────────────────────
    private static final int BTN_W       = 18;
    private static final int BTN_H       = 13;
    private static final int BTN_SPACING = 6;

    private record ATMButton(int index, int x, int y) {}
    private final List<ATMButton> buttons = new ArrayList<>();

    private static final String[][] KEYPAD_LABELS  = {{"1","2","3","V"},{"4","5","6","X"},{"7","8","9","0"}};
    private static final int[]      KEYPAD_U_OFFSET = {0,0,0,18, 0,0,0,36, 0,0,0,0};

    private record KeypadButton(String label, int x, int y, int uOffset) {}
    private final List<KeypadButton> keypadButtons = new ArrayList<>();

    // ── Machine à états ───────────────────────────────────────────────────────
    private enum State {
        LOADING, PIN_SETUP, PIN_SETUP_CONFIRM, PIN_ENTRY,
        MENU, BALANCE, TRANSFER_LIST, TRANSFER_AMOUNT, TRANSFER_CONFIRM,
        TRANSFER_DONE, HISTORY, MESSAGE,
        WITHDRAW_AMOUNT, WITHDRAW_PREVIEW, WITHDRAW_DONE,
        DEPOSIT_PREVIEW, DEPOSIT_DONE,
        STATEMENT_CONFIRM
    }

    private State state = State.LOADING;

    // ── Thème (persistant entre ouvertures) ───────────────────────────────────
    private static ATMTheme currentTheme = ATMTheme.BLEU;

    // ── Position du block (pour le son) ───────────────────────────────────────
    private final BlockPos atmPos;

    // ── Données de session ────────────────────────────────────────────────────
    private int cachedPin = -1;
    private int tempPin   = -1;
    private final StringBuilder pinInput = new StringBuilder();
    private final StringBuilder amountInput = new StringBuilder();

    private long balance = 0;
    private List<BankTransaction> history = new ArrayList<>();
    private List<BankSyncPacket.PlayerEntry> onlinePlayers = new ArrayList<>();

    private int playerScroll  = 0;
    private int historyScroll = 0;
    private int selectedPlayer = 0;

    private String feedbackMessage = "";
    private boolean lastActionOk = false;

    // ── Données Relevé ────────────────────────────────────────────────────────
    private record StmtHour(String label, long fromMs) {}
    private record StmtDay(String label, List<StmtHour> hours) {}
    private List<StmtDay> stmtDays = new ArrayList<>();
    private int stmtDayIdx  = 0;
    private int stmtHourIdx = 0;

    // ── Données Retrait / Dépôt ───────────────────────────────────────────────
    private java.util.List<CoinValue.CoinStack> withdrawBreakdown = null;
    private long depositPreviewTotal = 0;
    private java.util.List<CoinValue.CoinCount> depositPreviewCoins = new ArrayList<>();

    // ── Constructeur ──────────────────────────────────────────────────────────

    public ATMScreen(BlockPos atmPos) {
        super(TextComponent.EMPTY);
        this.atmPos = atmPos;
    }

    // ── Init & Layout ─────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        int bgX = this.width  / 2 - 128;
        int bgY = this.height / 2 - 128;

        // Boutons latéraux
        buttons.clear();
        for (int i = 0; i < 4; i++) {
            buttons.add(new ATMButton(i,bgX + 14,bgY + 82 + i * (BTN_H + BTN_SPACING)));
            buttons.add(new ATMButton(4 + i, bgX + 224, bgY + 82 + i * (BTN_H + BTN_SPACING)));
        }

        // Keypad
        keypadButtons.clear();
        int kStartX = bgX + 88;
        int kStartY = bgY + 198;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                String label = KEYPAD_LABELS[row][col];
                int xPos = kStartX + col * (BTN_W + 2) + (col == 3 ? 2 : 0);
                int yPos = kStartY + row * (BTN_H + 2);
                int uOffset = KEYPAD_U_OFFSET[col + row * 4];
                String displayLabel = (label.equals("V") || label.equals("X")) ? null : label;
                keypadButtons.add(new KeypadButton(displayLabel, xPos, yPos, uOffset));
            }
        }

        // Requête initiale au serveur (ignorée si l'écran est redimensionné)
        if (state == State.LOADING)
            sendAction(BankActionPacket.Action.GET_DATA, -1, null, 0);
    }

    // ── Réponse serveur ───────────────────────────────────────────────────────

    public void handleServerResponse(BankSyncPacket packet) {
        if (!packet.getTransactions().isEmpty()) history = packet.getTransactions();
        if (!packet.getOnlinePlayers().isEmpty()) onlinePlayers = packet.getOnlinePlayers();
        balance = packet.getBalance();
        feedbackMessage = packet.getMessage();

        switch (packet.getStatus()) {
            case OK -> state = packet.isHasPin() ? State.PIN_ENTRY : State.PIN_SETUP;

            case PIN_OK -> {
                if (cachedPin == -1 && state == State.PIN_SETUP) {
                    // Juste après setup : PIN créé = entré
                    cachedPin = tempPin;
                    tempPin = -1;
                }
                // Après VERIFY_PIN, cachedPin est déjà défini
                state = State.MENU;
                pinInput.setLength(0);
            }

            case PIN_INVALID -> {
                feedbackMessage = I18n.get("screen.cucubanymod.atm.error.pin_invalid");
                pinInput.setLength(0);
                // On reste dans PIN_ENTRY
            }

            case INSUFFICIENT_FUNDS -> {
                feedbackMessage = I18n.get("screen.cucubanymod.atm.error.insufficient");
                lastActionOk = false;
                state = State.TRANSFER_DONE;
            }

            case TRANSFER_OK -> { lastActionOk = true; state = State.TRANSFER_DONE; }

            case STATEMENT_OK -> {
                feedbackMessage = I18n.get("screen.cucubanymod.atm.statement.generated");
                state = State.MESSAGE;
            }

            case BALANCE_UPDATE -> balance = packet.getBalance();

            case WITHDRAW_OK -> state = State.WITHDRAW_DONE;

            case DEPOSIT_OK -> state = State.DEPOSIT_DONE;

            case ERROR -> state = State.MESSAGE;
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(PoseStack ps, int mouseX, int mouseY, float partialTick) {
        super.render(ps, mouseX, mouseY, partialTick);
        renderBg(ps);
        renderScreen(ps);
        renderButtons(ps, mouseX, mouseY);
        renderKeypadButtons(ps, mouseX, mouseY);
        renderButtonLabels(ps);
        renderContent(ps);
    }

    private void renderBg(PoseStack ps) {
        RenderSystem.setShaderTexture(0, atmTexture);
        blit(ps, this.width / 2 - 128, this.height / 2 - 128, 0, 0, 256, 256);
    }

    private void renderScreen(PoseStack ps) {
        int bgX = this.width  / 2 - 128;
        int bgY = this.height / 2 - 128;
        fill(ps, bgX + 37, bgY + 28, bgX + 219, bgY + 155, currentTheme.screenFill);
    }

    private void renderButtons(PoseStack ps, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, atmElements);
        for (ATMButton btn : buttons) {
            boolean hovered = isOver(mouseX, mouseY, btn.x(), btn.y(), BTN_W, BTN_H);
            blit(ps, btn.x(), btn.y(), 0, hovered ? BTN_H : 0, BTN_W, BTN_H, 64, 64);
        }
    }

    private void renderKeypadButtons(PoseStack ps, int mouseX, int mouseY) {
        for (KeypadButton btn : keypadButtons) {
            RenderSystem.setShaderTexture(0, atmElements);
            boolean hovered = isOver(mouseX, mouseY, btn.x(), btn.y(), BTN_W, BTN_H);
            blit(ps, btn.x(), btn.y(), btn.uOffset(), hovered ? BTN_H : 0, BTN_W, BTN_H, 64, 64);
            if (btn.label() != null) {
                int textX = btn.x() + BTN_W / 2;
                int textY = btn.y() + 2 + (hovered ? 1 : 0);
                drawCenteredString(ps, this.font, btn.label(), textX, textY, 0xFFFFFF);
            }
        }
    }

    private void renderButtonLabels(PoseStack ps) {
        int bgX = this.width  / 2 - 128;
        int bgY = this.height / 2 - 128;

        String[] leftLabels  = new String[4];
        String[] rightLabels = new String[4];

        // Définir les libellés selon l'état
        String btnBack    = I18n.get("screen.cucubanymod.atm.btn.back");
        String btnConfirm = I18n.get("screen.cucubanymod.atm.btn.confirm");
        String btnUp      = I18n.get("screen.cucubanymod.atm.btn.up");
        String btnDown    = I18n.get("screen.cucubanymod.atm.btn.down");
        switch (state) {
            case MENU -> {
                leftLabels[0]  = I18n.get("screen.cucubanymod.atm.btn.balance");
                leftLabels[1]  = I18n.get("screen.cucubanymod.atm.btn.transfer");
                leftLabels[2]  = I18n.get("screen.cucubanymod.atm.btn.history");
                leftLabels[3]  = I18n.get("screen.cucubanymod.atm.btn.statement");
                rightLabels[0] = I18n.get("screen.cucubanymod.atm.btn.withdraw");
                rightLabels[1] = I18n.get("screen.cucubanymod.atm.btn.deposit");
                rightLabels[2] = "< " + I18n.get("screen.cucubanymod.atm.theme." + currentTheme.name().toLowerCase()) + " >";
                rightLabels[3] = I18n.get("screen.cucubanymod.atm.btn.close");
            }
            case BALANCE -> rightLabels[2] = btnBack;
            case TRANSFER_LIST -> {
                leftLabels[0]  = I18n.get("screen.cucubanymod.atm.btn.choose");
                rightLabels[0] = btnUp;
                rightLabels[1] = btnDown;
                rightLabels[2] = btnBack;
            }
            case TRANSFER_AMOUNT -> {
                rightLabels[2] = btnBack;
                rightLabels[3] = I18n.get("screen.cucubanymod.atm.btn.clear");
            }
            case TRANSFER_CONFIRM -> {
                leftLabels[0]  = btnConfirm;
                rightLabels[2] = I18n.get("screen.cucubanymod.atm.btn.cancel");
            }
            case TRANSFER_DONE, MESSAGE, WITHDRAW_DONE, DEPOSIT_DONE ->
                rightLabels[3] = I18n.get("screen.cucubanymod.atm.btn.ok");
            case WITHDRAW_AMOUNT -> {
                leftLabels[0]  = "100";
                leftLabels[1]  = "500";
                leftLabels[2]  = "1 000";
                leftLabels[3]  = "5 000";
                rightLabels[0] = "10 000";
                rightLabels[1] = "50 000";
                rightLabels[2] = btnBack;
                rightLabels[3] = I18n.get("screen.cucubanymod.atm.btn.clear");
            }
            case WITHDRAW_PREVIEW -> {
                if (withdrawBreakdown != null) leftLabels[0] = btnConfirm;
                rightLabels[2] = btnBack;
            }
            case DEPOSIT_PREVIEW -> {
                if (depositPreviewTotal > 0) leftLabels[0] = btnConfirm;
                rightLabels[2] = btnBack;
            }
            case HISTORY -> {
                rightLabels[0] = btnUp;
                rightLabels[1] = btnDown;
                rightLabels[2] = btnBack;
            }
            case STATEMENT_CONFIRM -> {
                if (!stmtDays.isEmpty()) {
                    leftLabels[0]  = I18n.get("screen.cucubanymod.atm.statement.btn.prev_date");
                    rightLabels[0] = I18n.get("screen.cucubanymod.atm.statement.btn.next_date");
                    StmtDay day = stmtDays.get(stmtDayIdx);
                    if (day.hours().size() > 1) {
                        leftLabels[1]  = I18n.get("screen.cucubanymod.atm.statement.btn.prev_hour");
                        rightLabels[1] = I18n.get("screen.cucubanymod.atm.statement.btn.next_hour");
                    }
                    leftLabels[2] = I18n.get("screen.cucubanymod.atm.btn.confirm");
                }
                rightLabels[2] = btnBack;
            }
            case PIN_SETUP, PIN_SETUP_CONFIRM, PIN_ENTRY ->
                rightLabels[3] = I18n.get("screen.cucubanymod.atm.btn.delete");
        }

        float scale = 0.72f;
        for (int i = 0; i < 4; i++) {
            int btnY = bgY + 82 + i * (BTN_H + BTN_SPACING);
            int textY = btnY + 4;

            if (leftLabels[i] != null) {
                ps.pushPose();
                ps.translate(bgX + 39, textY, 0);
                ps.scale(scale, scale, 1f);
                this.font.draw(ps, leftLabels[i], 0, 0, currentTheme.buttonLabel);
                ps.popPose();
            }
            if (rightLabels[i] != null) {
                ps.pushPose();
                float labelW = this.font.width(rightLabels[i]) * scale;
                ps.translate(bgX + 216 - labelW, textY, 0);
                ps.scale(scale, scale, 1f);
                this.font.draw(ps, rightLabels[i], 0, 0, currentTheme.buttonLabel);
                ps.popPose();
            }
        }
    }

    /** Contenu de la zone écran (bgX+37 à bgX+218, bgY+28 à bgY+155). */
    private void renderContent(PoseStack ps) {
        int bgX = this.width  / 2 - 128;
        int bgY = this.height / 2 - 128;
        int sx  = bgX + 37;   // bord gauche de l'écran
        int sy  = bgY + 28;   // bord haut de l'écran
        int sw  = 181;        // largeur
        int cx  = sx + sw / 2;// centre X

        // Titre de l'écran
        drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.title", CucubanyCommonConfigs.BANK_NAME.get()).toUpperCase(), cx, sy + 4, currentTheme.header);
        // Séparateur
        fill(ps, sx + 4, sy + 14, sx + sw - 4, sy + 15, currentTheme.separator);

        int contentY = sy + 20;

        ATMTheme t = currentTheme;
        switch (state) {
            case LOADING -> drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.loading"), cx, contentY + 20, t.muted);

            case PIN_SETUP -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.pin_setup.title"), cx, contentY, t.primary);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.pin_setup.hint"), cx, contentY + 12, t.secondary);
                renderPinDots(ps, cx, contentY + 30);
            }

            case PIN_SETUP_CONFIRM -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.pin_confirm.title"), cx, contentY, t.primary);
                renderPinDots(ps, cx, contentY + 30);
            }

            case PIN_ENTRY -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.pin_entry.title"), cx, contentY, t.primary);
                renderPinDots(ps, cx, contentY + 30);
                if (!feedbackMessage.isEmpty())
                    drawCenteredString(ps, this.font, feedbackMessage, cx, contentY + 50, t.debit);
            }

            case MENU -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.solde", BankStatementItem.formatAmount(balance)), cx, contentY + 4, t.credit);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.menu.choose"), cx, contentY + 20, t.secondary);
            }

            case BALANCE -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.balance.title"), cx, contentY + 4, t.accent);
                fill(ps, sx + 20, contentY + 16, sx + sw - 20, contentY + 17, t.separator);
                drawCenteredString(ps, this.font, BankStatementItem.formatAmount(balance), cx, contentY + 26, t.credit);
            }

            case TRANSFER_LIST -> renderTransferList(ps, sx, contentY, cx);

            case TRANSFER_AMOUNT -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.transfer_amount.title"), cx, contentY, t.primary);
                if (selectedPlayer < onlinePlayers.size())
                    drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.transfer_amount.to", onlinePlayers.get(selectedPlayer).name()), cx, contentY + 12, t.secondary);
                drawCenteredString(ps, this.font, BankStatementItem.formatAmount(parseLong(amountInput)), cx, contentY + 30, t.highlight);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.transfer_amount.hint"), cx, contentY + 50, t.muted);
            }

            case TRANSFER_CONFIRM -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.transfer_confirm.title"), cx, contentY + 4, t.highlight);
                if (selectedPlayer < onlinePlayers.size())
                    drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.transfer_confirm.recipient", onlinePlayers.get(selectedPlayer).name()), cx, contentY + 18, t.primary);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.transfer_confirm.amount", BankStatementItem.formatAmount(parseLong(amountInput))), cx, contentY + 30, t.credit);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.transfer_confirm.remaining", BankStatementItem.formatAmount(balance - parseLong(amountInput))), cx, contentY + 44, t.secondary);
            }

            case TRANSFER_DONE -> {
                drawCenteredString(ps, this.font, lastActionOk ? I18n.get("screen.cucubanymod.atm.transfer_done.ok") : I18n.get("screen.cucubanymod.atm.transfer_done.fail"), cx, contentY + 10, lastActionOk ? t.credit : t.debit);
                renderWrapped(ps, feedbackMessage, sx + 10, contentY + 26, sw - 20, t.primary);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.solde", BankStatementItem.formatAmount(balance)), cx, contentY + 60, t.credit);
            }

            case HISTORY -> renderHistory(ps, sx, contentY, cx, sw);

            case MESSAGE -> renderWrappedCentered(ps, feedbackMessage, cx, contentY + 26, sw - 20, t.primary);

            case STATEMENT_CONFIRM -> renderStatementConfirm(ps, sx, contentY, cx, sw, t);

            case WITHDRAW_AMOUNT -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.withdraw.title"), cx, contentY, t.accent);
                fill(ps, sx + 4, contentY + 11, sx + sw - 4, contentY + 12, t.separator);
                String amtDisplay = !amountInput.isEmpty()
                    ? BankStatementItem.formatAmount(parseLong(amountInput)) : BankStatementItem.formatAmount(0);
                drawCenteredString(ps, this.font, amtDisplay, cx, contentY + 20, t.highlight);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.withdraw.hint"), cx, contentY + 35, t.muted);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.solde", BankStatementItem.formatAmount(balance)), cx, contentY + 50, t.secondary);
            }

            case WITHDRAW_PREVIEW -> renderWithdrawPreview(ps, sx, contentY, cx, sw, t);

            case WITHDRAW_DONE -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.withdraw.done"), cx, contentY + 10, t.credit);
                drawCenteredString(ps, this.font, feedbackMessage, cx, contentY + 28, t.primary);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.solde", BankStatementItem.formatAmount(balance)), cx, contentY + 50, t.credit);
            }

            case DEPOSIT_PREVIEW -> renderDepositPreview(ps, sx, contentY, cx, sw, t);

            case DEPOSIT_DONE -> {
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.deposit.done"), cx, contentY + 10, t.credit);
                drawCenteredString(ps, this.font, feedbackMessage, cx, contentY + 28, t.primary);
                drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.solde", BankStatementItem.formatAmount(balance)), cx, contentY + 50, t.credit);
            }
        }
    }

    private void renderPinDots(PoseStack ps, int cx, int y) {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            dots.append(i < pinInput.length() ? "■" : "□");
            if (i < 3) dots.append("  ");
        }
        drawCenteredString(ps, this.font, dots.toString(), cx, y, currentTheme.highlight);
    }

    private void renderTransferList(PoseStack ps, int sx, int contentY, int cx) {
        ATMTheme t = currentTheme;
        drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.transfer_list.title"), cx, contentY, t.primary);
        if (onlinePlayers.isEmpty()) {
            drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.transfer_list.empty"), cx, contentY + 25, t.muted);
            return;
        }
        int maxVisible = 5;
        for (int i = 0; i < maxVisible && (i + playerScroll) < onlinePlayers.size(); i++) {
            int idx = i + playerScroll;
            String name = onlinePlayers.get(idx).name();
            boolean selected = idx == selectedPlayer;
            String prefix = selected ? "> " : "  ";
            this.font.draw(ps, prefix + name, sx + 10, contentY + 14 + i * 10, selected ? t.highlight : t.primary);
        }
    }

    private void renderHistory(PoseStack ps, int sx, int contentY, int cx, int sw) {
        ATMTheme t = currentTheme;
        drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.history.title"), cx, contentY, t.accent);
        fill(ps, sx + 4, contentY + 11, sx + sw - 4, contentY + 12, t.separator);
        if (history.isEmpty()) {
            drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.history.empty"), cx, contentY + 25, t.muted);
            return;
        }

        float scale = 0.75f;
        // Marge droite pour éviter les labels de boutons (Haut/Bas/Retour ~40px)
        int contentRight = sx + sw - 44;
        int maxVisible = 7;

        for (int i = 0; i < maxVisible && (i + historyScroll) < history.size(); i++) {
            BankTransaction tx = history.get(i + historyScroll);
            int rowY = contentY + 14 + i * 9;
            boolean credit = tx.type().isCredit();
            String sign = credit ? "+" : "-";
            int amtColor = credit ? t.credit : t.debit;

            String amtStr = sign + BankStatementItem.formatAmount(tx.amount());
            float amtScreenW = this.font.width(amtStr) * scale;

            // Montant — aligné à droite de la zone sûre
            ps.pushPose();
            ps.translate(contentRight - amtScreenW, rowY, 0);
            ps.scale(scale, scale, 1f);
            this.font.draw(ps, amtStr, 0, 0, amtColor);
            ps.popPose();

            // Date + libellé — aligné à gauche, tronqué si nécessaire
            String dateLabel = tx.getFormattedDate() + " " + I18n.get(tx.type().getLabel());
            int maxLabelPx = (int) ((contentRight - amtScreenW - (sx + 6) - 4) / scale);
            String clipped = clipText(dateLabel, maxLabelPx);
            ps.pushPose();
            ps.translate(sx + 6, rowY, 0);
            ps.scale(scale, scale, 1f);
            this.font.draw(ps, clipped, 0, 0, currentTheme.primary);
            ps.popPose();
        }
    }

    private void renderWithdrawPreview(PoseStack ps, int sx, int contentY, int cx, int sw, ATMTheme t) {
        drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.withdraw.title") + " - " + BankStatementItem.formatAmount(parseLong(amountInput)), cx, contentY, t.accent);
        fill(ps, sx + 4, contentY + 11, sx + sw - 4, contentY + 12, t.separator);
        if (withdrawBreakdown == null) {
            drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.withdraw.impossible"), cx, contentY + 30, t.debit);
            drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.withdraw.impossible_hint"), cx, contentY + 42, t.muted);
        } else if (withdrawBreakdown.isEmpty()) {
            drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.withdraw.zero"), cx, contentY + 30, t.muted);
        } else {
            drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.withdraw.breakdown"), cx, contentY + 16, t.primary);
            float scale = 0.85f;
            // Marges pour éviter "Confirmer" (gauche) et "Retour" (droite)
            int contentLeft  = sx + 44;
            int contentRight = sx + sw - 44;
            int maxPx = (int) ((contentRight - contentLeft) / scale);
            for (int i = 0; i < withdrawBreakdown.size(); i++) {
                CoinValue.CoinStack cs = withdrawBreakdown.get(i);
                long coinVal = (long) cs.count() * cs.coin().getValue();
                String line = cs.count() + "x " + I18n.get(cs.coin().getLangKey())
                    + " (" + BankStatementItem.formatAmount(coinVal) + ")";
                ps.pushPose();
                ps.translate(contentLeft, contentY + 28 + i * 10, 0);
                ps.scale(scale, scale, 1f);
                this.font.draw(ps, clipText(line, maxPx), 0, 0, t.primary);
                ps.popPose();
            }
        }
    }

    private void renderDepositPreview(PoseStack ps, int sx, int contentY, int cx, int sw, ATMTheme t) {
        drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.deposit.title"), cx, contentY, t.accent);
        fill(ps, sx + 4, contentY + 11, sx + sw - 4, contentY + 12, t.separator);
        if (depositPreviewCoins.isEmpty()) {
            drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.deposit.empty"), cx, contentY + 30, t.muted);
        } else {
            float scale = 0.82f;
            // Marges pour éviter "Confirmer" (gauche) et "Retour" (droite)
            int contentLeft  = sx + 44;
            int contentRight = sx + sw - 44;
            int maxPx = (int) ((contentRight - contentLeft) / scale);
            int maxRows = Math.min(depositPreviewCoins.size(), 7);
            for (int i = 0; i < maxRows; i++) {
                CoinValue.CoinCount cc = depositPreviewCoins.get(i);
                long coinVal = cc.count() * cc.coin().getValue();
                String line = cc.count() + "x " + I18n.get(cc.coin().getLangKey())
                    + " (" + BankStatementItem.formatAmount(coinVal) + ")";
                ps.pushPose();
                ps.translate(contentLeft, contentY + 16 + i * 9, 0);
                ps.scale(scale, scale, 1f);
                this.font.draw(ps, clipText(line, maxPx), 0, 0, t.primary);
                ps.popPose();
            }
            fill(ps, sx + 4, contentY + 88, sx + sw - 4, contentY + 89, t.separator);
            drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.deposit.total", BankStatementItem.formatAmount(depositPreviewTotal)), cx, contentY + 92, t.credit);
        }
    }

    /** Construit la liste des jours/heures disponibles depuis l'historique. */
    private void buildStmtDays() {
        stmtDays.clear();
        stmtDayIdx  = 0;
        stmtHourIdx = 0;
        if (history.isEmpty()) return;

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
        // Map jour → liste de timestamps (une entrée par transaction)
        Map<LocalDate, List<Long>> dayTxMap = new LinkedHashMap<>();
        for (BankTransaction tx : history) {
            LocalDate date = LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.timestamp()), ZoneId.systemDefault()).toLocalDate();
            dayTxMap.computeIfAbsent(date, k -> new ArrayList<>()).add(tx.timestamp());
        }
        // Tri chronologique des jours
        List<LocalDate> sortedDays = new ArrayList<>(dayTxMap.keySet());
        sortedDays.sort(LocalDate::compareTo);

        for (LocalDate date : sortedDays) {
            List<Long> times = new ArrayList<>(dayTxMap.get(date));
            times.sort(Long::compareTo);
            List<StmtHour> hours = new ArrayList<>();
            long lastMinute = -1;
            for (long ts : times) {
                long minute = ts / 60_000;
                if (minute == lastMinute) continue; // dédoublonnage à la minute
                lastMinute = minute;
                LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
                hours.add(new StmtHour(String.format("%02dh%02d", dt.getHour(), dt.getMinute()), ts));
            }
            stmtDays.add(new StmtDay(date.format(dayFmt), hours));
        }
    }

    private void renderStatementConfirm(PoseStack ps, int sx, int contentY, int cx, int sw, ATMTheme t) {
        drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.statement.confirm.title"), cx, contentY + 4, t.accent);
        fill(ps, sx + 4, contentY + 14, sx + sw - 4, contentY + 15, t.separator);

        if (stmtDays.isEmpty()) {
            drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.history.empty"), cx, contentY + 35, t.muted);
            return;
        }

        StmtDay day  = stmtDays.get(stmtDayIdx);
        StmtHour hour = day.hours().get(stmtHourIdx);

        // "À partir de :"
        drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.statement.confirm.from"), cx, contentY + 22, t.secondary);

        // Date sélectionnée avec indicateurs de navigation
        String dateNav = (stmtDayIdx > 0 ? "◄ " : "  ") + day.label() + (stmtDayIdx < stmtDays.size() - 1 ? " ►" : "  ");
        drawCenteredString(ps, this.font, dateNav, cx, contentY + 36, t.highlight);

        // Heure sélectionnée (toujours affichée)
        String hourNav = (stmtHourIdx > 0 ? "◄ " : "  ") + hour.label() + (stmtHourIdx < day.hours().size() - 1 ? " ►" : "  ");
        drawCenteredString(ps, this.font, hourNav, cx, contentY + 52, day.hours().size() > 1 ? t.highlight : t.secondary);

        // Nombre de transactions incluses (centré en bas, hors zone boutons)
        long fromMs = hour.fromMs();
        int count = (int) history.stream().filter(tx -> tx.timestamp() >= fromMs).count();
        fill(ps, sx + 4, contentY + 90, sx + sw - 4, contentY + 91, t.separator);
        drawCenteredString(ps, this.font, I18n.get("screen.cucubanymod.atm.statement.confirm.count", count), cx, contentY + 96, t.primary);
    }

    private String clipText(String text, int maxWidthPx) {
        if (this.font.width(text) <= maxWidthPx) return text;
        String ellipsis = "..";
        int available = maxWidthPx - this.font.width(ellipsis);
        while (!text.isEmpty() && this.font.width(text) > available)
            text = text.substring(0, text.length() - 1);
        return text + ellipsis;
    }

    // ── Interactions ──────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX, my = (int) mouseY;

        // Boutons latéraux
        for (ATMButton btn : buttons) {
            if (isOver(mx, my, btn.x(), btn.y(), BTN_W, BTN_H)) {
                onSideButtonClicked(btn.index());
                return true;
            }
        }

        // Keypad
        for (int i = 0; i < keypadButtons.size(); i++) {
            KeypadButton btn = keypadButtons.get(i);
            if (isOver(mx, my, btn.x(), btn.y(), BTN_W, BTN_H)) {
                String label = KEYPAD_LABELS[i / 4][i % 4];
                onKeypadClicked(label);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playButtonSound() {
        if (atmPos != null)
            CucubanyPacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ATMSoundPacket(atmPos));
    }

    private void onSideButtonClicked(int index) {
        playButtonSound();
        switch (state) {
            case MENU -> {
                switch (index) {
                    case 0 -> state = State.BALANCE;
                    case 1 -> { playerScroll = 0; selectedPlayer = 0; state = State.TRANSFER_LIST; }
                    case 2 -> { historyScroll = 0; state = State.HISTORY; }
                    case 3 -> { buildStmtDays(); state = State.STATEMENT_CONFIRM; }
                    case 4 -> { amountInput.setLength(0); withdrawBreakdown = null; state = State.WITHDRAW_AMOUNT; }
                    case 5 -> {
                        var player = Minecraft.getInstance().player;
                        if (player != null) {
                            depositPreviewTotal = CoinValue.countInInventory(player);
                            depositPreviewCoins = CoinValue.listInInventory(player);
                        }
                        state = State.DEPOSIT_PREVIEW;
                    }
                    case 6 -> currentTheme = currentTheme.next();
                    case 7 -> this.onClose();
                }
            }
            case BALANCE      -> { if (index == 6) state = State.MENU; }
            case TRANSFER_LIST -> {
                switch (index) {
                    case 0 -> { if (!onlinePlayers.isEmpty()) { amountInput.setLength(0); state = State.TRANSFER_AMOUNT; } }
                    case 4 -> { if (playerScroll > 0) { playerScroll--; if (selectedPlayer > playerScroll) selectedPlayer = playerScroll; } }
                    case 5 -> { if (playerScroll + 1 < onlinePlayers.size()) { playerScroll++; if (selectedPlayer < playerScroll) selectedPlayer = playerScroll; } }
                    case 6 -> state = State.MENU;
                }
            }
            case TRANSFER_AMOUNT -> {
                switch (index) {
                    case 6 -> state = State.TRANSFER_LIST;
                    case 7 -> { if (amountInput.length() > 0) amountInput.deleteCharAt(amountInput.length() - 1); }
                }
            }
            case TRANSFER_CONFIRM -> {
                switch (index) {
                    case 0 -> confirmTransfer();
                    case 6 -> { amountInput.setLength(0); state = State.TRANSFER_LIST; }
                }
            }
            case TRANSFER_DONE, MESSAGE -> { if (index == 7) state = State.MENU; }
            case HISTORY -> {
                switch (index) {
                    case 4 -> { if (historyScroll > 0) historyScroll--; }
                    case 5 -> { if (historyScroll + 1 < history.size()) historyScroll++; }
                    case 6 -> state = State.MENU;
                }
            }
            case STATEMENT_CONFIRM -> {
                if (stmtDays.isEmpty()) { if (index == 6) state = State.MENU; break; }
                StmtDay day = stmtDays.get(stmtDayIdx);
                switch (index) {
                    case 0 -> { // date précédente
                        if (stmtDayIdx > 0) { stmtDayIdx--; stmtHourIdx = 0; }
                    }
                    case 4 -> { // date suivante
                        if (stmtDayIdx < stmtDays.size() - 1) { stmtDayIdx++; stmtHourIdx = 0; }
                    }
                    case 1 -> { // heure précédente
                        if (stmtHourIdx > 0) stmtHourIdx--;
                    }
                    case 5 -> { // heure suivante
                        if (stmtHourIdx < day.hours().size() - 1) stmtHourIdx++;
                    }
                    case 2 -> { // confirmer
                        long fromMs = stmtDays.get(stmtDayIdx).hours().get(stmtHourIdx).fromMs();
                        sendAction(BankActionPacket.Action.GET_STATEMENT, cachedPin, null, fromMs);
                    }
                    case 6 -> state = State.MENU;
                }
            }
            case WITHDRAW_AMOUNT -> {
                switch (index) {
                    case 0 -> setWithdrawAndPreview("100");
                    case 1 -> setWithdrawAndPreview("500");
                    case 2 -> setWithdrawAndPreview("1000");
                    case 3 -> setWithdrawAndPreview("5000");
                    case 4 -> setWithdrawAndPreview("10000");
                    case 5 -> setWithdrawAndPreview("50000");
                    case 6 -> state = State.MENU;
                    case 7 -> { if (!amountInput.isEmpty()) amountInput.deleteCharAt(amountInput.length() - 1); }
                }
            }
            case WITHDRAW_PREVIEW -> {
                switch (index) {
                    case 0 -> { if (withdrawBreakdown != null) confirmWithdraw(); }
                    case 6 -> state = State.WITHDRAW_AMOUNT;
                }
            }
            case WITHDRAW_DONE, DEPOSIT_DONE -> { if (index == 7) state = State.MENU; }
            case DEPOSIT_PREVIEW -> {
                switch (index) {
                    case 0 -> { if (depositPreviewTotal > 0) sendAction(BankActionPacket.Action.DEPOSIT, cachedPin, null, 0); }
                    case 6 -> state = State.MENU;
                }
            }
            case PIN_SETUP, PIN_SETUP_CONFIRM, PIN_ENTRY -> {
                if (index == 7 && pinInput.length() > 0)
                    pinInput.deleteCharAt(pinInput.length() - 1);
            }
        }
    }

    private void onKeypadClicked(String label) {
        playButtonSound();
        switch (label) {
            case "V" -> onValidate();
            case "X" -> onCancel();
            default  -> onDigit(label);
        }
    }

    private void onDigit(String digit) {
        switch (state) {
            case PIN_SETUP, PIN_SETUP_CONFIRM, PIN_ENTRY -> {
                if (pinInput.length() < 4) pinInput.append(digit);
            }
            case TRANSFER_AMOUNT, WITHDRAW_AMOUNT -> {
                if (amountInput.length() < 9) amountInput.append(digit);
            }
        }
    }

    private void onValidate() {
        switch (state) {
            case PIN_SETUP -> {
                if (pinInput.length() == 4) {
                    tempPin = Integer.parseInt(pinInput.toString());
                    pinInput.setLength(0);
                    state = State.PIN_SETUP_CONFIRM;
                }
            }
            case PIN_SETUP_CONFIRM -> {
                if (pinInput.length() == 4) {
                    int confirmed = Integer.parseInt(pinInput.toString());
                    if (confirmed == tempPin) {
                        cachedPin = tempPin;
                        sendAction(BankActionPacket.Action.SETUP_PIN, confirmed, null, 0);
                    } else {
                        feedbackMessage = I18n.get("screen.cucubanymod.atm.error.pin_mismatch");
                        tempPin = -1;
                        pinInput.setLength(0);
                        state = State.PIN_SETUP;
                    }
                }
            }
            case PIN_ENTRY -> {
                if (pinInput.length() == 4) {
                    int enteredPin = Integer.parseInt(pinInput.toString());
                    cachedPin = enteredPin;
                    sendAction(BankActionPacket.Action.VERIFY_PIN, enteredPin, null, 0);
                    pinInput.setLength(0);
                }
            }
            case TRANSFER_AMOUNT -> {
                long amount = parseLong(amountInput);
                if (amount > 0) state = State.TRANSFER_CONFIRM;
            }
            case TRANSFER_CONFIRM -> confirmTransfer();
            case WITHDRAW_AMOUNT -> {
                long amount = parseLong(amountInput);
                if (amount > 0) {
                    withdrawBreakdown = CoinValue.breakdown(amount);
                    state = State.WITHDRAW_PREVIEW;
                }
            }
        }
    }

    private void onCancel() {
        switch (state) {
            case PIN_SETUP, PIN_SETUP_CONFIRM, PIN_ENTRY -> pinInput.setLength(0);
            case TRANSFER_AMOUNT, WITHDRAW_AMOUNT -> amountInput.setLength(0);
            case TRANSFER_CONFIRM -> { amountInput.setLength(0); state = State.TRANSFER_LIST; }
        }
    }

    private void confirmTransfer() {
        if (selectedPlayer >= onlinePlayers.size()) return;
        UUID targetUUID = onlinePlayers.get(selectedPlayer).uuid();
        long amount = parseLong(amountInput);
        if (amount <= 0) return;
        sendAction(BankActionPacket.Action.TRANSFER, cachedPin, targetUUID, amount);
    }

    private void setWithdrawAndPreview(String value) {
        amountInput.setLength(0);
        amountInput.append(value);
        withdrawBreakdown = CoinValue.breakdown(parseLong(amountInput));
        state = State.WITHDRAW_PREVIEW;
    }

    private void confirmWithdraw() {
        long amount = parseLong(amountInput);
        if (amount <= 0 || withdrawBreakdown == null) return;
        sendAction(BankActionPacket.Action.WITHDRAW, cachedPin, null, amount);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void sendAction(BankActionPacket.Action action, int pin, UUID targetUUID, long amount) {
        CucubanyPacketHandler.INSTANCE.send(
            PacketDistributor.SERVER.noArg(),
            new BankActionPacket(action, pin, targetUUID, amount)
        );
    }

    private static boolean isOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static long parseLong(StringBuilder sb) {
        if (sb.length() == 0) return 0;
        try { return Long.parseLong(sb.toString()); }
        catch (NumberFormatException e) { return 0; }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (this.font.width(line + " " + word) > maxWidth) {
                lines.add(line.toString().trim());
                line = new StringBuilder(word);
            } else {
                if (!line.isEmpty()) line.append(" ");
                line.append(word);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString().trim());
        return lines;
    }

    private void renderWrapped(PoseStack ps, String text, int x, int y, int maxWidth, int color) {
        List<String> lines = wrapText(text, maxWidth);
        for (int i = 0; i < lines.size(); i++)
            this.font.draw(ps, lines.get(i), x, y + i * 10, color);
    }

    private void renderWrappedCentered(PoseStack ps, String text, int cx, int y, int maxWidth, int color) {
        List<String> lines = wrapText(text, maxWidth);
        for (int i = 0; i < lines.size(); i++)
            drawCenteredString(ps, this.font, lines.get(i), cx, y + i * 10, color);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
