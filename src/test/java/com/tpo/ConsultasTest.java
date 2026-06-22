package com.tpo;

import com.tpo.controller.ConsultaController;
import com.tpo.controller.FacturaController;
import com.tpo.controller.OrdenDeCompraController;
import com.tpo.controller.OrdenDePagoController;
import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.dto.ReporteDTO;
import com.tpo.model.entities.catalog.ItemProveedor;
import com.tpo.model.entities.catalog.Producto;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.document.DocumentoComercial;
import com.tpo.model.entities.document.Factura;
import com.tpo.model.entities.order.OrdenDeCompra;
import com.tpo.model.entities.payment.Efectivo;
import com.tpo.model.entities.supplier.Proveedor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica las Consultas Mínimas Obligatorias usando un proveedor único como sonda. */
class ConsultasTest {

    private static final String PROV = "ConsultaProbe SA";

    /** Mismo formato que usa la app (locale por defecto), para comparar de forma robusta. */
    private static String fmt(double valor) {
        return String.format("%.2f", valor);
    }

    @BeforeAll
    static void cargarDatos() {
        Rubro rubro = new Rubro("Insumos");
        Producto item = new Producto();
        item.setDescripcion("Barbijos");
        item.setRubro(rubro);

        Proveedor p = new Proveedor();
        p.setRazonSocial(PROV);
        p.setCondicionImpositiva(CondicionImpositiva.RESPONSABLE_INSCRIPTO);
        p.setLimiteDeudaAutorizado(100000);
        p.getRubros().add(rubro);
        p.getItemsProvistos().add(new ItemProveedor(50.0, item, p));

        // OC: 10 x 50 = 500
        OrdenDeCompra oc = new OrdenDeCompra(900, new Date(), p, null);
        oc.agregarItem(item, 10, 50.0);
        OrdenDeCompraController.getInstance().crearOrdenDeCompra(oc);

        // Factura registrada a partir de la OC (deuda 500)
        List<OrdenDeCompra> ordenes = new ArrayList<>();
        ordenes.add(oc);
        FacturaController.getInstance().generarFactura(p, ordenes, false, 50.0);

        // Pago de 500 (retenciones 30% = 150)
        Factura docPago = new Factura(p);
        docPago.setImporteTotal(500);
        List<DocumentoComercial> docs = new ArrayList<>();
        docs.add(docPago);
        OrdenDePagoController.getInstance().generarOrdenDePago(p, docs, new Efectivo(0f));

        // Segundo proveedor con factura SIN pagar -> queda PENDIENTE
        Rubro rubro2 = new Rubro("Limpieza");
        Producto item2 = new Producto();
        item2.setDescripcion("Detergente industrial");
        item2.setRubro(rubro2);

        Proveedor pendiente = new Proveedor();
        pendiente.setRazonSocial("PendienteProbe SA");
        pendiente.setCondicionImpositiva(CondicionImpositiva.RESPONSABLE_INSCRIPTO);
        pendiente.getRubros().add(rubro2);
        pendiente.getItemsProvistos().add(new ItemProveedor(100.0, item2, pendiente));

        OrdenDeCompra oc2 = new OrdenDeCompra(901, new Date(), pendiente, null);
        oc2.agregarItem(item2, 10, 100.0); // 1000, sin pago posterior
        OrdenDeCompraController.getInstance().crearOrdenDeCompra(oc2);
        List<OrdenDeCompra> ordenes2 = new ArrayList<>();
        ordenes2.add(oc2);
        FacturaController.getInstance().generarFactura(pendiente, ordenes2, false, 100.0);
    }

    private Object[] buscarFila(ReporteDTO r, String clave) {
        return r.getFilas().stream()
                .filter(f -> f.length > 0 && clave.equals(String.valueOf(f[0])))
                .findFirst().orElse(null);
    }

    @Test
    void ordenesDeCompraPorProveedor_incluyeLaSonda() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Órdenes de compra por proveedor");
        Object[] fila = buscarFila(r, PROV);
        assertNotNull(fila, "Debe existir fila para el proveedor sonda");
        assertEquals(fmt(500), String.valueOf(fila[2]));
    }

    @Test
    void pagosPorProveedor_incluyeLaSonda() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Pagos realizados por proveedor");
        Object[] fila = buscarFila(r, PROV);
        assertNotNull(fila);
        assertEquals(fmt(500), String.valueOf(fila[2])); // total bruto
        assertEquals(fmt(150), String.valueOf(fila[3])); // total retenido (21+6+3 %)
    }

    @Test
    void cuentaCorriente_calculaSaldo() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Cuenta corriente por proveedor");
        Object[] fila = buscarFila(r, PROV);
        assertNotNull(fila);
        assertEquals(fmt(0), String.valueOf(fila[3])); // facturado 500 - pagado 500
    }

    @Test
    void comparacionDePrecios_listaElItem() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Comparación de precios de ítems");
        boolean tieneBarbijos = r.getFilas().stream().anyMatch(f -> "Barbijos".equals(f[0]));
        assertTrue(tieneBarbijos);
    }

    @Test
    void totalRetenidoPorImpuesto_noVacio() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Total retenido por impuesto");
        assertFalse(r.getFilas().isEmpty(), "Debe haber retenciones discriminadas por impuesto");
    }

    @Test
    void pagosPorPeriodo_agrupaPorMes() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Pagos realizados por período");
        assertFalse(r.getFilas().isEmpty());
        // La primera columna debe tener formato yyyy-MM
        boolean formatoOk = r.getFilas().stream()
                .allMatch(f -> String.valueOf(f[0]).matches("\\d{4}-\\d{2}|\\(sin fecha\\)"));
        assertTrue(formatoOk, "El período debe estar en formato yyyy-MM");
    }

    @Test
    void totalRetenidoPorImpuestoYPeriodo_noVacio() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Total retenido por impuesto y período");
        assertFalse(r.getFilas().isEmpty());
    }

    @Test
    void documentosPorPeriodo_noVacio() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Documentos recibidos por período");
        assertFalse(r.getFilas().isEmpty());
    }

    @Test
    void pagosPorMedioDePago_incluyeEfectivo() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Pagos realizados por medio de pago");
        boolean tieneEfectivo = r.getFilas().stream().anyMatch(f -> "Efectivo".equals(f[0]));
        assertTrue(tieneEfectivo, "Debe agrupar el pago de la sonda bajo 'Efectivo'");
    }

    @Test
    void notas_impactanCuentaCorrienteSegunTipo() {
        com.tpo.model.entities.supplier.Proveedor p = new com.tpo.model.entities.supplier.Proveedor();
        p.setRazonSocial("NotaProbe SA");

        // Factura 1000 (deuda) + Nota de Débito 200 (suma) - Nota de Crédito 300 (resta) = 900
        Rubro rubro = new Rubro("Servicios");
        Producto item = new Producto();
        item.setDescripcion("Mantenimiento");
        item.setRubro(rubro);
        p.getRubros().add(rubro);
        p.getItemsProvistos().add(new ItemProveedor(100.0, item, p));
        OrdenDeCompra oc = new OrdenDeCompra(950, new Date(), p, null);
        oc.agregarItem(item, 10, 100.0);
        OrdenDeCompraController.getInstance().crearOrdenDeCompra(oc);
        List<OrdenDeCompra> ordenes = new ArrayList<>();
        ordenes.add(oc);
        FacturaController.getInstance().generarFactura(p, ordenes, false, 100.0); // 1000

        com.tpo.controller.NotaController.getInstance()
                .registrarNota(com.tpo.model.dto.NotaDTO.DEBITO, p, 8001, 200f, new Date());
        com.tpo.controller.NotaController.getInstance()
                .registrarNota(com.tpo.model.dto.NotaDTO.CREDITO, p, 8002, 300f, new Date());

        ReporteDTO r = ConsultaController.getInstance().ejecutar("Cuenta corriente por proveedor");
        Object[] fila = buscarFila(r, "NotaProbe SA");
        assertNotNull(fila);
        assertEquals(fmt(900), String.valueOf(fila[1])); // deuda generada: 1000 + 200 - 300
    }

    @Test
    void documentosPendientesDePago_listaPendienteYNoLosCancelados() {
        ReporteDTO r = ConsultaController.getInstance().ejecutar("Documentos pendientes de pago");

        Object[] pendiente = buscarFila(r, "PendienteProbe SA");
        assertNotNull(pendiente, "El documento impago debe aparecer");
        assertEquals("PENDIENTE", String.valueOf(pendiente[5]));

        // El proveedor sonda pagó el 100% de su factura -> no debe figurar como pendiente
        Object[] cancelado = buscarFila(r, PROV);
        org.junit.jupiter.api.Assertions.assertNull(cancelado, "Un documento cancelado no debe figurar");
    }
}
