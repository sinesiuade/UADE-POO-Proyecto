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
 * Persistencia simple en archivos JSON (carpeta {@code data/} en la raíz del proyecto).
 * Cada controlador guarda y carga su lista de registros planos con Gson.
 *
 * <p>El guardado se puede desactivar con la propiedad de sistema {@code app.persistence=off}
 * (los tests lo usan para no sobrescribir los datos semilla).</p>
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

    /** Carga una lista desde {@code data/<archivo>}; devuelve lista vacía si no existe o falla. */
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

    /** Guarda {@code data} en {@code data/<archivo>} (salvo que la persistencia esté desactivada). */
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
