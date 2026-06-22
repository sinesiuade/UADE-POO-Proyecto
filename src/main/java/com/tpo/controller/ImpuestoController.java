package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.entities.tax.Impuesto;
import com.tpo.model.entities.tax.TipoImpuesto;
import com.tpo.util.JsonStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controlador Singleton de Impuestos. Mantiene la lista de impuestos vigentes del sistema,
 * que el flujo de Generación de Orden de Pago recorre para calcular las retenciones.
 * Persiste en {@code data/impuestos.json}.
 */
public class ImpuestoController {

    private static final String ARCHIVO = "impuestos.json";

    private final List<Impuesto> impuestos;

    private ImpuestoController() {
        impuestos = JsonStore.load(ARCHIVO, new TypeToken<List<Impuesto>>() {}.getType());
        if (impuestos.isEmpty()) {
            // Retenciones por defecto si no hay datos persistidos
            impuestos.add(new Impuesto(TipoImpuesto.IVA, 21.0));
            impuestos.add(new Impuesto(TipoImpuesto.GANANCIAS, 6.0));
            impuestos.add(new Impuesto(TipoImpuesto.IIBB, 3.0));
        }
    }

    private static ImpuestoController INSTANCE;

    public static ImpuestoController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImpuestoController();
        }
        return INSTANCE;
    }

    public void agregarImpuesto(Impuesto impuesto) {
        impuestos.add(impuesto);
        persistir();
    }

    public void editarImpuesto(int index, Impuesto impuesto) {
        impuestos.set(index, impuesto);
        persistir();
    }

    public List<Impuesto> getImpuestos() {
        return Collections.unmodifiableList(impuestos);
    }

    private void persistir() {
        JsonStore.save(ARCHIVO, impuestos);
    }
}
