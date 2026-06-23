package com.tpo.controller;

import com.tpo.model.Enums.Rol;
import com.tpo.model.entities.user.Usuario;

/** Controlador Singleton de la sesión actual (usuario y rol en memoria). */
public class SesionController {

    private Usuario usuarioActual = new Usuario("Operador", Rol.OPERADOR);

    private SesionController() {
    }

    private static SesionController INSTANCE;

    public static SesionController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SesionController();
        }
        return INSTANCE;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuario) {
        if (usuario != null) {
            this.usuarioActual = usuario;
        }
    }

    /** El usuario de la sesión tiene rol supervisor. */
    public boolean esSupervisor() {
        return usuarioActual != null && usuarioActual.esSupervisor();
    }
}
