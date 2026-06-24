package com.tpo.controller;

import com.tpo.model.dto.FacturaDTO;
import com.tpo.model.dto.NotaDTO;
import com.tpo.model.dto.OrdenDeCompraDTO;
import com.tpo.model.dto.OrdenDePagoDTO;
import com.tpo.model.dto.ReporteDTO;
import com.tpo.model.entities.order.DetalleItemOC;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.entities.tax.TipoImpuesto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Controlador Singleton de Consultas y Reportes. Lee de los demás controladores. */
public class ConsultaController {

    private final Map<String, Supplier<ReporteDTO>> reportes = new LinkedHashMap<>();

    private ConsultaController() {
        // 1. Trazabilidad Documental (Facturas, Notas de Crédito y Débito)
        reportes.put("Documentos recibidos por proveedor", this::documentosPorProveedor);
        reportes.put("Documentos recibidos por tipo", this::documentosPorTipo);
        reportes.put("Documentos recibidos por período", this::documentosPorPeriodo);
        reportes.put("Documentos recibidos (detalle por fecha)", this::documentosDetalle);
        // 2. Gestión de Deuda (Cuenta Corriente)
        reportes.put("Cuenta corriente por proveedor", this::cuentaCorrientePorProveedor);
        reportes.put("Cuenta corriente (movimientos cronológicos)", this::cuentaCorrienteCronologica);
        reportes.put("Deuda actual por proveedor", this::deudaActualPorProveedor);
        reportes.put("Documentos pendientes de pago", this::documentosPendientesDePago);
        // 3. Seguimiento de Compras y Pagos
        reportes.put("Órdenes de compra por estado", this::ordenesDeCompraPorEstado);
        reportes.put("Órdenes de compra por rubro", this::ordenesDeCompraPorRubro);
        reportes.put("Órdenes de compra por proveedor", this::ordenesDeCompraPorProveedor);
        reportes.put("Pagos realizados por proveedor", this::pagosPorProveedor);
        reportes.put("Pagos realizados por período", this::pagosPorPeriodo);
        reportes.put("Pagos realizados por medio de pago", this::pagosPorMedioDePago);
        // 4. Análisis de Costos y Precios
        reportes.put("Comparación de precios de ítems", this::comparacionDePrecios);
        // 5. Reportes Fiscales
        reportes.put("Total retenido por impuesto", this::totalRetenidoPorImpuesto);
        reportes.put("Total retenido por impuesto y período", this::totalRetenidoPorImpuestoYPeriodo);
        reportes.put("Libro IVA Compras", this::libroIvaCompras);
    }

    private static ConsultaController INSTANCE;

    public static ConsultaController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConsultaController();
        }
        return INSTANCE;
    }

    public java.util.Set<String> getNombresDeReportes() {
        return reportes.keySet();
    }

    public ReporteDTO ejecutar(String nombre) {
        Supplier<ReporteDTO> reporte = reportes.get(nombre);
        return reporte != null ? reporte.get() : new ReporteDTO("Reporte no encontrado", new String[]{"-"});
    }

    private static String nombre(Proveedor p) {
        return p != null && p.getRazonSocial() != null ? p.getRazonSocial() : "(sin proveedor)";
    }

    /** Período mensual (yyyy-MM) de una fecha. */
    private static String periodo(java.util.Date fecha) {
        return fecha != null ? new java.text.SimpleDateFormat("yyyy-MM").format(fecha) : "(sin fecha)";
    }

    // --- 1. Trazabilidad Documental -------------------------------------------------------

    /** Documento recibido normalizado (Factura o Nota). */
    private static class DocRecibido {
        final Date fecha;
        final String proveedor;
        final double importe;
        final String tipo;
        final String estado;

        DocRecibido(Date fecha, String proveedor, double importe, String tipo, String estado) {
            this.fecha = fecha;
            this.proveedor = proveedor;
            this.importe = importe;
            this.tipo = tipo;
            this.estado = estado;
        }
    }

    /** Facturas + Notas unificadas. */
    private List<DocRecibido> documentosRecibidos() {
        List<DocRecibido> docs = new ArrayList<>();
        for (FacturaDTO f : FacturaController.getInstance().getFacturas()) {
            docs.add(new DocRecibido(f.getFechaEmision(), nombre(f.getProveedor()), f.getImporteTotal(),
                    "Factura", f.isObservada() ? "Observada" : "Registrada"));
        }
        for (NotaDTO n : NotaController.getInstance().getNotas()) {
            docs.add(new DocRecibido(n.getFechaEmision(), nombre(n.getProveedor()), n.getImporteTotal(),
                    n.getTipo(), "Registrada"));
        }
        return docs;
    }

    private ReporteDTO documentosPorProveedor() {
        ReporteDTO r = new ReporteDTO("Documentos recibidos por proveedor",
                new String[]{"Proveedor", "Cantidad", "Total"});
        Map<String, double[]> acc = new LinkedHashMap<>(); // [cantidad, total]
        for (DocRecibido d : documentosRecibidos()) {
            double[] v = acc.computeIfAbsent(d.proveedor, k -> new double[2]);
            v[0]++;
            v[1] += d.importe;
        }
        acc.forEach((prov, v) -> r.agregarFila(prov, (int) v[0], String.format("%.2f", v[1])));
        return r;
    }

    private ReporteDTO documentosPorTipo() {
        ReporteDTO r = new ReporteDTO("Documentos recibidos por tipo",
                new String[]{"Tipo", "Cantidad", "Total"});
        Map<String, double[]> acc = new LinkedHashMap<>();
        for (DocRecibido d : documentosRecibidos()) {
            double[] v = acc.computeIfAbsent(d.tipo, k -> new double[2]);
            v[0]++;
            v[1] += d.importe;
        }
        acc.forEach((tipo, v) -> r.agregarFila(tipo, (int) v[0], String.format("%.2f", v[1])));
        return r;
    }

    private ReporteDTO documentosDetalle() {
        ReporteDTO r = new ReporteDTO("Documentos recibidos (detalle)",
                new String[]{"Fecha", "Tipo", "Proveedor", "Importe", "Estado"});
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        documentosRecibidos().stream()
                .sorted(Comparator.comparing(d -> d.fecha, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(d -> r.agregarFila(
                        d.fecha != null ? sdf.format(d.fecha) : "",
                        d.tipo,
                        d.proveedor,
                        String.format("%.2f", d.importe),
                        d.estado));
        return r;
    }

    private ReporteDTO documentosPorPeriodo() {
        ReporteDTO r = new ReporteDTO("Documentos recibidos por período",
                new String[]{"Período", "Cantidad", "Total"});
        Map<String, double[]> acc = new java.util.TreeMap<>(); // ordenado por período
        for (DocRecibido d : documentosRecibidos()) {
            double[] v = acc.computeIfAbsent(periodo(d.fecha), k -> new double[2]);
            v[0]++;
            v[1] += d.importe;
        }
        acc.forEach((per, v) -> r.agregarFila(per, (int) v[0], String.format("%.2f", v[1])));
        return r;
    }

    // --- 2. Gestión de Deuda --------------------------------------------------------------

    private Map<String, double[]> calcularSaldos() {
        // [deudaGenerada, pagado] -- deuda = Facturas + ND - NC
        Map<String, double[]> acc = new LinkedHashMap<>();
        for (FacturaDTO f : FacturaController.getInstance().getFacturas()) {
            acc.computeIfAbsent(nombre(f.getProveedor()), k -> new double[2])[0] += f.getImporteTotal();
        }
        for (NotaDTO n : NotaController.getInstance().getNotas()) {
            acc.computeIfAbsent(nombre(n.getProveedor()), k -> new double[2])[0] += n.getImpactoDeuda();
        }
        for (OrdenDePagoDTO p : OrdenDePagoController.getInstance().getPagos()) {
            acc.computeIfAbsent(nombre(p.getProveedor()), k -> new double[2])[1] += p.getTotalBrutoPagado();
        }
        return acc;
    }

    private ReporteDTO cuentaCorrientePorProveedor() {
        ReporteDTO r = new ReporteDTO("Cuenta corriente por proveedor",
                new String[]{"Proveedor", "Deuda Generada", "Total Pagado", "Saldo"});
        calcularSaldos().forEach((prov, v) ->
                r.agregarFila(prov, String.format("%.2f", v[0]), String.format("%.2f", v[1]),
                        String.format("%.2f", v[0] - v[1])));
        return r;
    }

    /** Movimiento de cuenta corriente: cargo (Debe) o pago/crédito (Haber). */
    private static class Movimiento {
        final String proveedor;
        final Date fecha;
        final String tipo;
        final double debe;
        final double haber;

        Movimiento(String proveedor, Date fecha, String tipo, double debe, double haber) {
            this.proveedor = proveedor;
            this.fecha = fecha;
            this.tipo = tipo;
            this.debe = debe;
            this.haber = haber;
        }
    }

    private ReporteDTO cuentaCorrienteCronologica() {
        ReporteDTO r = new ReporteDTO("Cuenta corriente (movimientos cronológicos)",
                new String[]{"Proveedor", "Fecha", "Tipo", "Debe", "Haber", "Saldo"});

        List<Movimiento> movs = new ArrayList<>();
        for (FacturaDTO f : FacturaController.getInstance().getFacturas()) {
            movs.add(new Movimiento(nombre(f.getProveedor()), f.getFechaEmision(), "Factura", f.getImporteTotal(), 0));
        }
        for (NotaDTO n : NotaController.getInstance().getNotas()) {
            boolean credito = NotaDTO.CREDITO.equals(n.getTipo());
            movs.add(new Movimiento(nombre(n.getProveedor()), n.getFechaEmision(), n.getTipo(),
                    credito ? 0 : n.getImporteTotal(), credito ? n.getImporteTotal() : 0));
        }
        for (OrdenDePagoDTO p : OrdenDePagoController.getInstance().getPagos()) {
            movs.add(new Movimiento(nombre(p.getProveedor()), p.getFechaEmision(), "Pago", 0, p.getTotalBrutoPagado()));
        }

        // Ordena por proveedor y luego por fecha (cronológico)
        movs.sort(Comparator.comparing((Movimiento m) -> m.proveedor)
                .thenComparing(m -> m.fecha, Comparator.nullsLast(Comparator.naturalOrder())));

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String provActual = null;
        double saldo = 0;
        for (Movimiento m : movs) {
            if (!m.proveedor.equals(provActual)) {
                provActual = m.proveedor;
                saldo = 0; // reinicia el saldo corriente por proveedor
            }
            saldo += m.debe - m.haber;
            r.agregarFila(m.proveedor,
                    m.fecha != null ? sdf.format(m.fecha) : "",
                    m.tipo,
                    String.format("%.2f", m.debe),
                    String.format("%.2f", m.haber),
                    String.format("%.2f", saldo));
        }
        return r;
    }

    private ReporteDTO deudaActualPorProveedor() {
        ReporteDTO r = new ReporteDTO("Deuda actual por proveedor",
                new String[]{"Proveedor", "Deuda Actual"});
        calcularSaldos().forEach((prov, v) -> {
            double saldo = v[0] - v[1];
            if (saldo > 0) {
                r.agregarFila(prov, String.format("%.2f", saldo));
            }
        });
        return r;
    }

    private ReporteDTO documentosPendientesDePago() {
        ReporteDTO r = new ReporteDTO("Documentos pendientes de pago (ordenado por antigüedad)",
                new String[]{"Proveedor", "Fecha", "Importe", "Pagado", "Saldo", "Estado", "Antigüedad (días)"});
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        long hoy = System.currentTimeMillis();

        List<FacturaDTO> pendientes = new java.util.ArrayList<>();
        for (FacturaDTO f : FacturaController.getInstance().getFacturas()) {
            if (f.getSaldo() > 0.0001) {
                pendientes.add(f);
            }
        }
        // Más antiguos primero (mayor antigüedad)
        pendientes.sort(java.util.Comparator.comparing(FacturaDTO::getFechaEmision,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));

        for (FacturaDTO f : pendientes) {
            long dias = f.getFechaEmision() != null ? (hoy - f.getFechaEmision().getTime()) / 86_400_000L : 0;
            r.agregarFila(
                    nombre(f.getProveedor()),
                    f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "",
                    String.format("%.2f", f.getImporteTotal()),
                    String.format("%.2f", f.getMontoPagado()),
                    String.format("%.2f", f.getSaldo()),
                    f.getEstadoCancelacion(),
                    dias);
        }
        return r;
    }

    // --- 3. Seguimiento de Compras y Pagos ------------------------------------------------

    private ReporteDTO ordenesDeCompraPorEstado() {
        ReporteDTO r = new ReporteDTO("Órdenes de compra por estado",
                new String[]{"Estado", "Cantidad", "Total Bruto"});
        Map<String, double[]> acc = new LinkedHashMap<>();
        for (OrdenDeCompraDTO oc : OrdenDeCompraController.getInstance().getOrdenes()) {
            String estado = oc.getEstado() != null ? oc.getEstado().toString() : "(sin estado)";
            double[] v = acc.computeIfAbsent(estado, k -> new double[2]);
            v[0]++;
            v[1] += oc.getTotalBruto();
        }
        acc.forEach((estado, v) -> r.agregarFila(estado, (int) v[0], String.format("%.2f", v[1])));
        return r;
    }

    private ReporteDTO ordenesDeCompraPorRubro() {
        ReporteDTO r = new ReporteDTO("Órdenes de compra por rubro",
                new String[]{"Rubro", "Cantidad de OC", "Total"});
        Map<String, java.util.Set<Integer>> ocsPorRubro = new LinkedHashMap<>();
        Map<String, Double> totalPorRubro = new LinkedHashMap<>();
        for (OrdenDeCompraDTO oc : OrdenDeCompraController.getInstance().getOrdenes()) {
            if (oc.getDetalleItems() == null) {
                continue;
            }
            for (DetalleItemOC d : oc.getDetalleItems()) {
                String rubro = d.getItem() != null && d.getItem().getRubro() != null
                        && d.getItem().getRubro().getNombre() != null
                        ? d.getItem().getRubro().getNombre() : "(sin rubro)";
                ocsPorRubro.computeIfAbsent(rubro, k -> new java.util.HashSet<>()).add(oc.getNumero());
                totalPorRubro.merge(rubro, d.getPrecioTotal(), Double::sum);
            }
        }
        ocsPorRubro.forEach((rubro, ocs) ->
                r.agregarFila(rubro, ocs.size(), String.format("%.2f", totalPorRubro.get(rubro))));
        return r;
    }

    private ReporteDTO ordenesDeCompraPorProveedor() {
        ReporteDTO r = new ReporteDTO("Órdenes de compra por proveedor",
                new String[]{"Proveedor", "Cantidad", "Total Bruto"});
        Map<String, double[]> acc = new LinkedHashMap<>();
        for (OrdenDeCompraDTO oc : OrdenDeCompraController.getInstance().getOrdenes()) {
            double[] v = acc.computeIfAbsent(nombre(oc.getProveedor()), k -> new double[2]);
            v[0]++;
            v[1] += oc.getTotalBruto();
        }
        acc.forEach((prov, v) -> r.agregarFila(prov, (int) v[0], String.format("%.2f", v[1])));
        return r;
    }

    private ReporteDTO pagosPorProveedor() {
        ReporteDTO r = new ReporteDTO("Pagos realizados por proveedor",
                new String[]{"Proveedor", "Cantidad", "Total Bruto", "Total Retenido", "Total Neto"});
        Map<String, double[]> acc = new LinkedHashMap<>(); // [cant, bruto, retenido, neto]
        for (OrdenDePagoDTO p : OrdenDePagoController.getInstance().getPagos()) {
            double[] v = acc.computeIfAbsent(nombre(p.getProveedor()), k -> new double[4]);
            v[0]++;
            v[1] += p.getTotalBrutoPagado();
            v[2] += p.getTotalRetenido();
            v[3] += p.getTotalNetoAPagar();
        }
        acc.forEach((prov, v) -> r.agregarFila(prov, (int) v[0],
                String.format("%.2f", v[1]), String.format("%.2f", v[2]), String.format("%.2f", v[3])));
        return r;
    }

    private ReporteDTO pagosPorPeriodo() {
        ReporteDTO r = new ReporteDTO("Pagos realizados por período",
                new String[]{"Período", "Cantidad", "Total Bruto", "Total Retenido", "Total Neto"});
        Map<String, double[]> acc = new java.util.TreeMap<>(); // [cant, bruto, retenido, neto]
        for (OrdenDePagoDTO p : OrdenDePagoController.getInstance().getPagos()) {
            double[] v = acc.computeIfAbsent(periodo(p.getFechaEmision()), k -> new double[4]);
            v[0]++;
            v[1] += p.getTotalBrutoPagado();
            v[2] += p.getTotalRetenido();
            v[3] += p.getTotalNetoAPagar();
        }
        acc.forEach((per, v) -> r.agregarFila(per, (int) v[0],
                String.format("%.2f", v[1]), String.format("%.2f", v[2]), String.format("%.2f", v[3])));
        return r;
    }

    private ReporteDTO pagosPorMedioDePago() {
        ReporteDTO r = new ReporteDTO("Pagos realizados por medio de pago",
                new String[]{"Medio de Pago", "Cantidad", "Total Neto"});
        Map<String, double[]> acc = new LinkedHashMap<>(); // [cantidad, neto]
        for (OrdenDePagoDTO p : OrdenDePagoController.getInstance().getPagos()) {
            String medio = p.getMedioDePago() != null ? p.getMedioDePago() : "(sin medio)";
            double[] v = acc.computeIfAbsent(medio, k -> new double[2]);
            v[0]++;
            v[1] += p.getTotalNetoAPagar();
        }
        acc.forEach((medio, v) -> r.agregarFila(medio, (int) v[0], String.format("%.2f", v[1])));
        return r;
    }

    // --- 4. Análisis de Costos y Precios --------------------------------------------------

    private ReporteDTO comparacionDePrecios() {
        ReporteDTO r = new ReporteDTO("Comparación de precios de ítems (desde las OC)",
                new String[]{"Ítem", "Proveedor", "Precio Acordado"});
        for (OrdenDeCompraDTO oc : OrdenDeCompraController.getInstance().getOrdenes()) {
            String prov = nombre(oc.getProveedor());
            if (oc.getDetalleItems() == null) {
                continue;
            }
            for (DetalleItemOC d : oc.getDetalleItems()) {
                String item = d.getItem() != null && d.getItem().getDescripcion() != null
                        ? d.getItem().getDescripcion() : "(sin descripción)";
                r.agregarFila(item, prov, String.format("%.2f", d.getPrecioAcordado()));
            }
        }
        r.getFilas().sort(java.util.Comparator
                .comparing((Object[] f) -> (String) f[0])
                .thenComparing(f -> (String) f[2]));
        return r;
    }

    // --- 5. Reportes Fiscales -------------------------------------------------------------

    private ReporteDTO totalRetenidoPorImpuesto() {
        ReporteDTO r = new ReporteDTO("Total retenido por impuesto",
                new String[]{"Impuesto", "Total Retenido"});
        Map<TipoImpuesto, Double> acc = new LinkedHashMap<>();
        for (OrdenDePagoDTO p : OrdenDePagoController.getInstance().getPagos()) {
            if (p.getRetencionesPorImpuesto() != null) {
                p.getRetencionesPorImpuesto().forEach((tipo, monto) -> acc.merge(tipo, monto, Double::sum));
            }
        }
        acc.forEach((tipo, monto) -> r.agregarFila(tipo, String.format("%.2f", monto)));
        return r;
    }

    private ReporteDTO totalRetenidoPorImpuestoYPeriodo() {
        ReporteDTO r = new ReporteDTO("Total retenido por impuesto y período",
                new String[]{"Período", "Impuesto", "Total Retenido"});
        // período -> (impuesto -> monto), ordenado por período
        Map<String, Map<TipoImpuesto, Double>> acc = new java.util.TreeMap<>();
        for (OrdenDePagoDTO p : OrdenDePagoController.getInstance().getPagos()) {
            if (p.getRetencionesPorImpuesto() == null) {
                continue;
            }
            Map<TipoImpuesto, Double> porImpuesto =
                    acc.computeIfAbsent(periodo(p.getFechaEmision()), k -> new LinkedHashMap<>());
            p.getRetencionesPorImpuesto().forEach((tipo, monto) -> porImpuesto.merge(tipo, monto, Double::sum));
        }
        acc.forEach((per, porImpuesto) ->
                porImpuesto.forEach((tipo, monto) -> r.agregarFila(per, tipo, String.format("%.2f", monto))));
        return r;
    }

    /** Libro IVA Compras: una fila por (factura, alícuota) con CUIT, neto, IVA y total. */
    private ReporteDTO libroIvaCompras() {
        ReporteDTO r = new ReporteDTO("Libro IVA Compras",
                new String[]{"Fecha", "CUIT", "Proveedor", "Tipo", "Alícuota IVA", "Neto Gravado", "IVA", "Total"});
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        List<FacturaDTO> facturas = new ArrayList<>(FacturaController.getInstance().getFacturas());
        facturas.sort(Comparator.comparing(FacturaDTO::getFechaEmision,
                Comparator.nullsLast(Comparator.naturalOrder())));

        for (FacturaDTO f : facturas) {
            String razon = nombre(f.getProveedor());
            Proveedor real = ProveedorController.getInstance().getProveedorPorRazonSocial(razon);
            String cuit = real != null && real.getCuit() > 0 ? String.valueOf(real.getCuit()) : "-";
            String fecha = f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "";

            if (f.getIvaPorAlicuota() == null || f.getIvaPorAlicuota().isEmpty()) {
                // Sin desglose disponible: muestra el total como una sola fila informativa.
                r.agregarFila(fecha, cuit, razon, "Factura", "-",
                        String.format("%.2f", f.getImporteTotal()), "0.00",
                        String.format("%.2f", f.getImporteTotal()));
                continue;
            }
            for (FacturaDTO.IvaAlicuota ia : f.getIvaPorAlicuota()) {
                r.agregarFila(fecha, cuit, razon, "Factura",
                        String.format("%.1f%%", ia.getAlicuota()),
                        String.format("%.2f", ia.getNeto()),
                        String.format("%.2f", ia.getIva()),
                        String.format("%.2f", ia.getNeto() + ia.getIva()));
            }
        }
        return r;
    }
}
