package fr.cucubany.cucubanymod.items.advanced;

import fr.cucubany.cucubanymod.CucubanyMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IdentityCardNumberManager {
    private static Path IDENTITY_NUMBER_FILE = null;

    public static void registerWorldData(Path worldPath) {
        IDENTITY_NUMBER_FILE = Paths.get(worldPath.toString(), "data", "cucubany", "identity_number.txt");

    }

    public static synchronized int getNextIdentityNumber() {
        if(IDENTITY_NUMBER_FILE == null) {
            return -1;
        }
        int identityNumber = 0;
        if (Files.exists(IDENTITY_NUMBER_FILE)) {
            try {
                identityNumber = Integer.parseInt(Files.readString(IDENTITY_NUMBER_FILE));
            } catch (IOException | NumberFormatException e) {
                return -1;
            }
        }

        try {
            Path parent = IDENTITY_NUMBER_FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(IDENTITY_NUMBER_FILE, String.valueOf(identityNumber + 1));
        } catch (IOException e) {
            CucubanyMod.getLogger().error("Failed to write identity number to file " + e.getMessage());
        }

        return identityNumber;
    }
}
