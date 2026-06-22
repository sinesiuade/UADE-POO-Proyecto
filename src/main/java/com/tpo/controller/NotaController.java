package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.dto.NotaDTO;
import com.tpo.model.entities.document.DocumentoComercial;
import com.tpo.model.entities.document.NotaDeCredito;
import com.tpo.model.entities.document.NotaDeDebito;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.persistence.NotaRecord;
import com.tpo.util.JsonStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Controlador Singleton de Notas de Crédito / Débito recibidas.
 * Modela la herencia de DocumentoComercial: la Nota de Débito incrementa la deuda y la
 * Nota de Crédito la disminuye (polimorfismo de getImpactoCuentaCorriente).
 * Persiste en {@code data/notas.json}.
 */
public class NotaController {

    private static final String ARCHIVO = "notas.json";

    private final List<NotaDTO> notas = new ArrayList<>();

    private NotaController() {
        List<NotaRecord> registros = JsonStore.load(ARCHIVO, new TypeToken<List<NotaRecord>>() {}.getType());
        for (NotaRecord r : registros) {
            notas.add(fromRecord(r));
        }
    }

    private static NotaController INSTANCE;

    public static NotaController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NotaController();
        }
        return INSTANCE;
    }

    public NotaDTO registrarNota(String tipo, Proveedor proveedor, int numero, float importe, Date fecha) {
        DocumentoComercial documento = crearDocumento(tipo, proveedor, numero, importe, fecha);
        NotaDTO dto = new NotaDTO(proveedor, fecha, numero, importe, tipo);
        notas.add(dto);
        persistir();
        return dto;
    }

    public NotaDTO editarNota(int index, String tipo, Proveedor proveedor, int numero, float importe, Date fecha) {
        crearDocumento(tipo, proveedor, numero, importe, fecha); // valida/instancia el documento
        NotaDTO dto = new NotaDTO(proveedor, fecha, numero, importe, tipo);
        notas.set(index, dto);
        persistir();
        return dto;
    }

    public List<NotaDTO> getNotas() {
        return Collections.unmodifiableList(notas);
    }

    /** Instancia el documento concreto según el tipo (herencia: NotaDeCredito / NotaDeDebito). */
    private DocumentoComercial crearDocumento(String tipo, Proveedor proveedor, int numero, float importe, Date fecha) {
        return NotaDTO.CREDITO.equals(tipo)
                ? new NotaDeCredito(proveedor, numero, importe, fecha)
                : new NotaDeDebito(proveedor, numero, importe, fecha);
    }

    private void persistir() {
        List<NotaRecord> registros = new ArrayList<>();
        for (NotaDTO dto : notas) {
            registros.add(toRecord(dto));
        }
        JsonStore.save(ARCHIVO, registros);
    }

    private NotaRecord toRecord(NotaDTO dto) {
        NotaRecord r = new NotaRecord();
        r.proveedor = dto.getProveedor() != null ? dto.getProveedor().getRazonSocial() : null;
        r.fecha = dto.getFechaEmision() != null ? dto.getFechaEmision().getTime() : 0;
        r.numero = dto.getNumero();
        r.importe = dto.getImporteTotal();
        r.tipo = dto.getTipo();
        return r;
    }

    private NotaDTO fromRecord(NotaRecord r) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(r.proveedor);
        return new NotaDTO(p, new Date(r.fecha), r.numero, r.importe, r.tipo);
    }
}
