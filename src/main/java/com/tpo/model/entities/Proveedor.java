package com.tpo.model.entities;

import com.tpo.model.Enums.CondicionImpositiva;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<CertificadoDeExclusion> certificadosDeExclusion = new ArrayList<>();
    private List<Rubro> rubros = new ArrayList<>();

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

    public List<CertificadoDeExclusion> obtenerCertificadosDeExclusionVigentes() {
        return new ArrayList<>(certificadosDeExclusion);
    }

    public double getDeudaActual() {
        return 0.0;
    }

    public void crearOrdenDeCompra() {
    }

    public List<Rubro> getListaRubros() {
        return rubros;
    }
}
