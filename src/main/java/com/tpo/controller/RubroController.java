package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.util.JsonStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Controlador Singleton del catálogo de Rubros de prestación (clasificación). */
public class RubroController {

    private static final String ARCHIVO = "rubros.json";

    /** Catálogo predefinido de rubros (según el enunciado del TPO). */
    private static final String[] CATALOGO = {
            "Laboratorio medicinal",
            "Estudios de diagnóstico por imágenes",
            "Servicio de ambulancias y traslados",
            "Limpieza y desinfección (general y técnica)",
            "Seguridad y vigilancia",
            "Mantenimiento edilicio (electricidad, plomería, gas)",
            "Insumos descartables y estériles",
            "Tecnología médica y equipamiento (venta y service)"
    };

    private final List<Rubro> rubros;

    private RubroController() {
        rubros = JsonStore.load(ARCHIVO, new TypeToken<List<Rubro>>() {}.getType());
        if (rubros.isEmpty()) {
            for (String nombre : CATALOGO) {
                rubros.add(new Rubro(nombre));
            }
            persistir();
        }
    }

    private static RubroController INSTANCE;

    public static RubroController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RubroController();
        }
        return INSTANCE;
    }

    public List<Rubro> getRubros() {
        return Collections.unmodifiableList(rubros);
    }

    /** Agrega un rubro al catálogo si su nombre no existe ya. */
    public void agregarRubro(Rubro rubro) {
        if (rubro == null || rubro.getNombre() == null || existeRubro(rubro.getNombre())) {
            return;
        }
        rubros.add(rubro);
        persistir();
    }

    /** Indica si ya existe un rubro con ese nombre (ignorando mayúsculas/minúsculas). */
    public boolean existeRubro(String nombre) {
        if (nombre == null) {
            return false;
        }
        return rubros.stream().anyMatch(r -> nombre.equalsIgnoreCase(r.getNombre()));
    }

    private void persistir() {
        JsonStore.save(ARCHIVO, rubros);
    }
}
