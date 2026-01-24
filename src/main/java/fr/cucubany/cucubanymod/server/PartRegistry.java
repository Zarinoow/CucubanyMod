package fr.cucubany.cucubanymod.server;

import fr.cucubany.cucubanymod.hitbox.BodyPartEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PartRegistry {
    // Stocke ID -> Entité. Thread-safe pour éviter les crashs.
    private static final Map<Integer, BodyPartEntity> parts = new ConcurrentHashMap<>();

    public static void add(BodyPartEntity part) {
        parts.put(part.getId(), part);
    }

    public static void remove(BodyPartEntity part) {
        parts.remove(part.getId());
    }

    public static BodyPartEntity get(int id) {
        return parts.get(id);
    }
}