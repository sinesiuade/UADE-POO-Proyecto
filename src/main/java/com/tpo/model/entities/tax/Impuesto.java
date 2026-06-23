package com.tpo.model.entities.tax;

import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.entities.supplier.Proveedor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Impuesto {

    private TipoImpuesto nombre;
    private double porcentaje;
    /** Mínimo No Imponible: si el bruto no lo supera, no se retiene (umbral todo-o-nada). */
    private double minimoNoImponible;

    public Impuesto(TipoImpuesto nombre, double porcentaje) {
        this.nombre = nombre;
        this.porcentaje = porcentaje;
    }

    /** Retención del impuesto sobre el bruto, si corresponde aplicarlo. */
    public double calcularRetencion(Proveedor proveedor, double totalBruto) {
        CondicionImpositiva condicion = proveedor.getCondicionImpositiva();
        List<CertificadoDeExclusion> certificados = proveedor.getListaCertificadosDeExclusion();

        if (!validarAplicacion(condicion, certificados)) {
            return 0.0;
        }
        // Mínimo No Imponible: no se retiene si la base no supera el umbral.
        if (totalBruto <= minimoNoImponible) {
            return 0.0;
        }
        return totalBruto * (porcentaje / 100.0);
    }

    /** No aplica si el proveedor es Exento/Consumidor Final o tiene certificado vigente. */
    public boolean validarAplicacion(CondicionImpositiva condicion, List<CertificadoDeExclusion> certificados) {
        if (condicion == CondicionImpositiva.IVA_EXENTO || condicion == CondicionImpositiva.CONSUMIDOR_FINAL) {
            return false;
        }
        if (certificados != null) {
            Date hoy = new Date();
            for (CertificadoDeExclusion certificado : certificados) {
                if (certificado.aplicaA(nombre) && certificado.estaVigente(hoy)) {
                    return false;
                }
            }
        }
        return true;
    }
}
