package com.tpo.model.services;

import com.tpo.model.entities.Ejemplo;
import com.tpo.model.repositories.EjemploRepository;
import com.tpo.util.Validator;

import java.util.List;

public class EjemploService {

    private final EjemploRepository repository;

    public EjemploService(EjemploRepository repository) {
        this.repository = repository;
    }

    public Ejemplo crear(String nombre) {
        Validator.validarNoVacio(nombre, "El nombre no puede estar vacío");
        Ejemplo ejemplo = new Ejemplo(null, nombre.trim());
        return repository.guardar(ejemplo);
    }

    public List<Ejemplo> listarTodos() {
        return repository.listarTodos();
    }
}
