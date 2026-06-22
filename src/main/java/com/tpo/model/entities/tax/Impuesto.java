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

    /**
     * Calcula la retención de este impuesto para un proveedor sobre un monto bruto.
     * Sigue el Diagrama de Secuencia "Generación de Orden de Pago": consulta la
     * condición impositiva y los certificados de exclusión del proveedor y, si
     * corresponde aplicar, retiene el porcentaje configurado.
     */
    public double calcularRetencion(Proveedor proveedor, double totalBruto) {
        CondicionImpositiva condicion = proveedor.getCondicionImpositiva();
        List<CertificadoDeExclusion> certificados = proveedor.getListaCertificadosDeExclusion();

        if (!validarAplicacion(condicion, certificados)) {
            return 0.0;
        }
        return totalBruto * (porcentaje / 100.0);
    }

    /**
     * Determina si el impuesto debe aplicarse. No se aplica si el proveedor es
     * IVA Exento / Consumidor Final, o si tiene un certificado de exclusión
     * vigente para este tipo de impuesto.
     */
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
