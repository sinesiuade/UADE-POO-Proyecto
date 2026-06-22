package com.tpo.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia en archivos JSON (carpeta {@code data/}) con Gson.
 * El guardado se desactiva con {@code -Dapp.persistence=off} (usado por los tests).
 */
public final class JsonStore {

    private static final Logger log = LoggerFactory.getLogger(JsonStore.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DATA_DIR = Paths.get("data");

    private JsonStore() {
    }

    private static boolean persistenciaDesactivada() {
        return "off".equalsIgnoreCase(System.getProperty("app.persistence"));
    }

    /** Carga una lista desde {@code data/<archivo>} (vacía si no existe). */
    public static <T> List<T> load(String archivo, Type listType) {
        Path path = DATA_DIR.resolve(archivo);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<T> data = GSON.fromJson(reader, listType);
            return data != null ? data : new ArrayList<>();
        } catch (Exception e) {
            log.warn("No se pudo leer {}: {}", archivo, e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Guarda {@code data} en {@code data/<archivo>}. */
    public static void save(String archivo, Object data) {
        if (persistenciaDesactivada()) {
            return;
        }
        try {
            Files.createDirectories(DATA_DIR);
            try (Writer writer = Files.newBufferedWriter(DATA_DIR.resolve(archivo), StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            log.warn("No se pudo guardar {}: {}", archivo, e.getMessage());
        }
    }
}
