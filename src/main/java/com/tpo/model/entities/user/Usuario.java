package com.tpo.model.entities.user;

import com.tpo.model.Enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private String nombre;
    private Rol rol;

    /** Habilitado para las acciones que requieren rol supervisor. */
    public boolean esSupervisor() {
        return rol == Rol.SUPERVISOR;
    }
}
