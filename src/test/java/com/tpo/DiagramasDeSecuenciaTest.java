package com.tpo;

import com.tpo.controller.FacturaController;
import com.tpo.controller.OrdenDeCompraController;
import com.tpo.controller.OrdenDePagoController;
import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.Enums.EstadoOrdenDeCompra;
import com.tpo.model.dto.FacturaDTO;
import com.tpo.model.dto.OrdenDeCompraDTO;
import com.tpo.model.dto.OrdenDePagoDTO;
import com.tpo.model.entities.catalog.ItemProveedor;
import com.tpo.model.entities.catalog.Producto;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.document.DocumentoComercial;
import com.tpo.model.entities.document.Factura;
import com.tpo.model.entities.order.OrdenDeCompra;
import com.tpo.model.entities.supplier.Proveedor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica los 3 Diagramas de Secuencia de la Entrega 1 contra la implementación. */
class DiagramasDeSecuenciaTest {

    private Proveedor proveedor(CondicionImpositiva condicion, int limite, double deuda) {
        Proveedor p = new Proveedor();
        p.setRazonSocial("Proveedor SA");
        p.setCondicionImpositiva(condicion);
        p.setLimiteDeudaAutorizado(limite);
        p.setDeudaActual(deuda);
        return p;
    }

    @Test
    void creacionOC_dentroDelLimite_quedaEmitida() {
        Proveedor p = proveedor(CondicionImpositiva.RESPONSABLE_INSCRIPTO, 1000, 500);
        OrdenDeCompra oc = new OrdenDeCompra(1, new Date(), p, null);
        oc.agregarItem(new Producto(), 1, 400); // deuda 500 + 400 = 900 <= 1000

        OrdenDeCompraDTO dto = OrdenDeCompraController.getInstance().crearOrdenDeCompra(oc);

        assertEquals(EstadoOrdenDeCompra.EMITIDA, dto.getEstado());
        assertEquals(400.0, dto.getTotalBruto(), 0.001);
    }

    @Test
    void creacionOC_superaLimite_quedaPendienteAprobacion() {
        Proveedor p = proveedor(CondicionImpositiva.RESPONSABLE_INSCRIPTO, 1000, 500);
        OrdenDeCompra oc = new OrdenDeCompra(2, new Date(), p, null);
        oc.agregarItem(new Producto(), 1, 600); // 500 + 600 = 1100 > 1000

        OrdenDeCompraDTO dto = OrdenDeCompraController.getInstance().crearOrdenDeCompra(oc);

        assertEquals(EstadoOrdenDeCompra.PENDIENTE_APROBACION, dto.getEstado());
    }

    @Test
    void generacionOP_responsableInscripto_aplicaRetenciones() {
        Proveedor p = proveedor(CondicionImpositiva.RESPONSABLE_INSCRIPTO, 0, 0);
        Factura doc = new Factura(p);
        doc.setImporteTotal(1000);
        List<DocumentoComercial> docs = new ArrayList<>();
        docs.add(doc);

        OrdenDePagoDTO dto = OrdenDePagoController.getInstance().generarOrdenDePago(p, docs);

        // Retenciones por defecto: IVA 21% + Ganancias 6% + IIBB 3% = 300 sobre 1000
        assertEquals(1000.0, dto.getTotalBrutoPagado(), 0.001);
        assertEquals(300.0, dto.getTotalRetenido(), 0.001);
        assertEquals(700.0, dto.getTotalNetoAPagar(), 0.001);
    }

    @Test
    void generacionOP_consumidorFinal_noRetiene() {
        Proveedor p = proveedor(CondicionImpositiva.CONSUMIDOR_FINAL, 0, 0);
        Factura doc = new Factura(p);
        doc.setImporteTotal(1000);
        List<DocumentoComercial> docs = new ArrayList<>();
        docs.add(doc);

        OrdenDePagoDTO dto = OrdenDePagoController.getInstance().generarOrdenDePago(p, docs);

        assertEquals(0.0, dto.getTotalRetenido(), 0.001);
        assertEquals(1000.0, dto.getTotalNetoAPagar(), 0.001);
    }

    @Test
    void generacionFactura_preciosCoinciden_noObservada() {
        FacturaDTO dto = facturaConOC(100.0, 100.0);
        assertFalse(dto.isObservada());
        assertEquals(200.0, dto.getImporteTotal(), 0.001); // cantidad 2 * 100
    }

    @Test
    void generacionFactura_preciosDifieren_quedaObservada() {
        FacturaDTO dto = facturaConOC(100.0, 120.0);
        assertTrue(dto.isObservada());
    }

    @Test
    void generacionFactura_sinOCniAutorizacion_falla() {
        Proveedor p = proveedor(CondicionImpositiva.RESPONSABLE_INSCRIPTO, 0, 0);
        assertThrows(IllegalStateException.class,
                () -> FacturaController.getInstance().generarFactura(p, new ArrayList<>(), false, 0));
    }

    private FacturaDTO facturaConOC(double precioOC, double precioFactura) {
        Rubro rubro = new Rubro("Insumos");
        Producto item = new Producto();
        item.setDescripcion("Guantes");
        item.setRubro(rubro);

        Proveedor p = proveedor(CondicionImpositiva.RESPONSABLE_INSCRIPTO, 0, 0);
        p.getRubros().add(rubro);
        p.getItemsProvistos().add(new ItemProveedor(precioOC, item, p));

        OrdenDeCompra oc = new OrdenDeCompra(10, new Date(), p, null);
        oc.agregarItem(item, 2, precioOC);
        List<OrdenDeCompra> ordenes = new ArrayList<>();
        ordenes.add(oc);

        return FacturaController.getInstance().generarFactura(p, ordenes, false, precioFactura);
    }
}
