package com.tpo.model.entities.supplier;

import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.entities.catalog.Item;
import com.tpo.model.entities.catalog.ItemProveedor;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.tax.CertificadoDeExclusion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Proveedor {

    private float cuit;
    private String razonSocial;
    private String nombreComercial;
    private String domicilio;
    private float telefono;
    private String email;
    private CondicionImpositiva condicionImpositiva;
    private float nroInscripcionFiscal;
    private Date fechaInicioAct;
    private int limiteDeudaAutorizado;
    private double deudaActual;
    private List<CertificadoDeExclusion> certificadosDeExclusion = new ArrayList<>();
    private List<Rubro> rubros = new ArrayList<>();
    private List<ItemProveedor> itemsProvistos = new ArrayList<>();

    public Proveedor(float cuit, String razonSocial, String nombreComercial, String domicilio,
                     float telefono, String email, CondicionImpositiva condicionImpositiva,
                     float nroInscripcionFiscal, Date fechaInicioAct, int limiteDeudaAutorizado) {
        this.cuit = cuit;
        this.razonSocial = razonSocial;
        this.nombreComercial = nombreComercial;
        this.domicilio = domicilio;
        this.telefono = telefono;
        this.email = email;
        this.condicionImpositiva = condicionImpositiva;
        this.nroInscripcionFiscal = nroInscripcionFiscal;
        this.fechaInicioAct = fechaInicioAct;
        this.limiteDeudaAutorizado = limiteDeudaAutorizado;
    }

    /** Certificados de exclusión del proveedor (la vigencia la evalúa cada Impuesto). */
    public List<CertificadoDeExclusion> getListaCertificadosDeExclusion() {
        return certificadosDeExclusion;
    }

    /** El proveedor está habilitado para operar en el rubro indicado. */
    public boolean perteneceARubro(Rubro rubro) {
        if (rubro == null) {
            return false;
        }
        return rubros.stream().anyMatch(r -> r.getNombre() != null && r.getNombre().equals(rubro.getNombre()));
    }

    /** El proveedor está autorizado a suministrar el ítem indicado (relación N:M vía ItemProveedor). */
    public boolean proveeItem(Item item) {
        if (item == null) {
            return false;
        }
        return itemsProvistos.stream().anyMatch(ip -> ip.getItemBase() == item);
    }
}
