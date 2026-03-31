package fr.cucubany.cucubanymod.client.screen;

/** Thèmes visuels de l'ATM. Tous les champs sont des couleurs ARGB. */
public enum ATMTheme {

    //                 nom        fill        separ.      header      primary     secondary   accent      highlight   credit      debit       muted       btnLabel
    BLEU    ("Bleu",   0xFF3A465E, 0xFF4488CC, 0xFF88CCFF, 0xFFCCEEFF, 0xFF88AACC, 0xFF66FFAA, 0xFFFFFF44, 0xFF44FF88, 0xFFFF6644, 0xFFAAAAAA, 0xFFAADDFF),
    TERMINAL("Terminal",0xFF0D1A0D,0xFF00AA00, 0xFF00FF41, 0xFF00CC33, 0xFF007722, 0xFF00FF00, 0xFFAAFF00, 0xFF00FF41, 0xFFFF3300, 0xFF336633, 0xFF00CC44),
    RETRO("Retro",  0xFF1A1200, 0xFFCC7700, 0xFFFFAA00, 0xFFFF8800, 0xFFCC6600, 0xFFFFCC00, 0xFFFFFF44, 0xFFFFAA00, 0xFFFF3300, 0xFF664400, 0xFFFFAA44),
    MINUIT  ("Minuit", 0xFF1A0D2E, 0xFF7744BB, 0xFFCC88FF, 0xFFAA66EE, 0xFF7744AA, 0xFFFF88FF, 0xFFFFAA44, 0xFF88FFCC, 0xFFFF4466, 0xFF552266, 0xFFCC88FF),
    ECARLATE("Écarlate",0xFF2A0808,0xFFAA2222, 0xFFFF6644, 0xFFFF8866, 0xFFCC4422, 0xFFFFAA44, 0xFFFFFF88, 0xFFAAFF44, 0xFFFF2222, 0xFF662222, 0xFFFF9977);

    public final String displayName;
    public final int screenFill, separator, header, primary, secondary, accent, highlight, credit, debit, muted, buttonLabel;

    ATMTheme(String displayName, int screenFill, int separator, int header, int primary,
             int secondary, int accent, int highlight, int credit, int debit, int muted, int buttonLabel) {
        this.displayName  = displayName;
        this.screenFill   = screenFill;
        this.separator    = separator;
        this.header       = header;
        this.primary      = primary;
        this.secondary    = secondary;
        this.accent       = accent;
        this.highlight    = highlight;
        this.credit       = credit;
        this.debit        = debit;
        this.muted        = muted;
        this.buttonLabel  = buttonLabel;
    }

    public ATMTheme next() {
        ATMTheme[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
