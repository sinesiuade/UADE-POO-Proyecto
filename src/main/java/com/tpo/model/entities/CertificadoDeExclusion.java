package com.tpo.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoDeExclusion {

    private Impuesto impuesto;
    private Date fechaDesde;
    private Date fechaHasta;
    private int numeroCertificado;
}
