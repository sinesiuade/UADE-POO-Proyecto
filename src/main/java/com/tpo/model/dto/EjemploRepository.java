package com.tpo.model.repositories;

import com.tpo.model.entities.Ejemplo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class EjemploRepository {

    private final List<Ejemplo> ejemplos = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public Ejemplo guardar(Ejemplo ejemplo) {
        if (ejemplo.getId() == null) {
            ejemplo.setId(secuencia.getAndIncrement());
            ejemplos.add(ejemplo);
        } else {
            ejemplos.stream()
                    .filter(e -> e.getId().equals(ejemplo.getId()))
                    .findFirst()
                    .ifPresent(e -> e.setNombre(ejemplo.getNombre()));
        }
        return ejemplo;
    }

    public List<Ejemplo> listarTodos() {
        return List.copyOf(ejemplos);
    }

    public Optional<Ejemplo> buscarPorId(Long id) {
        return ejemplos.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }
}
