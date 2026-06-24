package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.entities.catalog.Item;
import com.tpo.model.entities.catalog.Producto;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.catalog.Servicio;
import com.tpo.model.persistence.ItemRecord;
import com.tpo.util.JsonStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** Controlador Singleton del catálogo de Ítems (Bienes y Prestaciones). */
public class ItemController {

    private static final String ARCHIVO = "items.json";
    public static final String PRODUCTO = "Producto";
    public static final String SERVICIO = "Servicio";

    private final List<Item> items = new ArrayList<>();

    private ItemController() {
        List<ItemRecord> registros = JsonStore.load(ARCHIVO, new TypeToken<List<ItemRecord>>() {}.getType());
        for (ItemRecord r : registros) {
            items.add(fromRecord(r));
        }
        if (items.isEmpty()) {
            seedDatosDePrueba();
            persistir();
        }
    }

    private static ItemController INSTANCE;

    public static ItemController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemController();
        }
        return INSTANCE;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Devuelve el Item del catálogo con ese código, o null si no existe. */
    public Item getItemPorCodigo(int codigo) {
        for (Item item : items) {
            if (item.getCodigo() == codigo) {
                return item;
            }
        }
        return null;
    }

    public void agregarItem(Item item) {
        items.add(item);
        persistir();
    }

    public void editarItem(int index, Item item) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        items.set(index, item);
        persistir();
    }

    /** Datos de prueba iniciales (2 Productos, 2 Servicios) con rubros del catálogo del PDF. */
    private void seedDatosDePrueba() {
        Producto guantes = new Producto();
        configurarComun(guantes, 1, "Guantes de látex", "Insumos descartables y estériles", "Caja x100", 5000.0, 21.0f);
        guantes.setLote("L-001");
        guantes.setVencimiento(new Date());
        guantes.setStock("120");
        items.add(guantes);

        Producto alcohol = new Producto();
        configurarComun(alcohol, 2, "Alcohol en gel 250ml", "Insumos descartables y estériles", "Unidad", 1200.0, 21.0f);
        alcohol.setLote("L-002");
        alcohol.setVencimiento(new Date());
        alcohol.setStock("300");
        items.add(alcohol);

        Servicio resonancia = new Servicio();
        configurarComun(resonancia, 3, "Resonancia magnética", "Estudios de diagnóstico por imágenes", "Estudio", 80000.0, 10.5f);
        resonancia.setTipoDePrestacion("Diagnóstico por imágenes");
        items.add(resonancia);

        Servicio limpieza = new Servicio();
        configurarComun(limpieza, 4, "Servicio de limpieza mensual", "Limpieza y desinfección (general y técnica)", "Mes", 150000.0, 21.0f);
        limpieza.setTipoDePrestacion("Limpieza y desinfección");
        items.add(limpieza);
    }

    private void configurarComun(Item item, int codigo, String descripcion, String rubro,
                                 String unidadMedida, double precioUnitario, float porcentajeIva) {
        item.setCodigo(codigo);
        item.setDescripcion(descripcion);
        item.setRubro(new Rubro(rubro));
        item.setUnidadMedida(unidadMedida);
        item.setPrecioUnitario(precioUnitario);
        item.setPorcentajeIva(porcentajeIva);
    }

    private void persistir() {
        List<ItemRecord> registros = new ArrayList<>();
        for (Item item : items) {
            registros.add(toRecord(item));
        }
        JsonStore.save(ARCHIVO, registros);
    }

    private ItemRecord toRecord(Item item) {
        ItemRecord r = new ItemRecord();
        r.codigo = item.getCodigo();
        r.descripcion = item.getDescripcion();
        r.rubro = item.getRubro() != null ? item.getRubro().getNombre() : null;
        r.unidadMedida = item.getUnidadMedida();
        r.precioUnitario = item.getPrecioUnitario();
        r.porcentajeIva = item.getPorcentajeIva();
        if (item instanceof Producto p) {
            r.tipo = PRODUCTO;
            r.lote = p.getLote();
            r.vencimiento = p.getVencimiento() != null ? p.getVencimiento().getTime() : 0;
            r.stock = p.getStock();
        } else if (item instanceof Servicio s) {
            r.tipo = SERVICIO;
            r.tipoDePrestacion = s.getTipoDePrestacion();
        }
        return r;
    }

    private Item fromRecord(ItemRecord r) {
        Item item;
        if (SERVICIO.equals(r.tipo)) {
            Servicio s = new Servicio();
            s.setTipoDePrestacion(r.tipoDePrestacion);
            item = s;
        } else {
            Producto p = new Producto();
            p.setLote(r.lote);
            p.setVencimiento(r.vencimiento > 0 ? new Date(r.vencimiento) : null);
            p.setStock(r.stock);
            item = p;
        }
        item.setCodigo(r.codigo);
        item.setDescripcion(r.descripcion);
        item.setRubro(r.rubro != null ? new Rubro(r.rubro) : null);
        item.setUnidadMedida(r.unidadMedida);
        item.setPrecioUnitario(r.precioUnitario);
        item.setPorcentajeIva((float) r.porcentajeIva);
        return item;
    }
}
