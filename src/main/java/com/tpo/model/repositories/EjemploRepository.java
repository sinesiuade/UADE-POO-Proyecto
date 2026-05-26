package com.tpo.model.repositories;

import com.tpo.model.entities.Ejemplo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class EjemploRepository {

    private static final Logger log = LoggerFactory.getLogger(EjemploRepository.class);

    private final List<Ejemplo> ejemplos = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public Ejemplo guardar(Ejemplo ejemplo) {
        if (ejemplo.getId() == null) {
            ejemplo.setId(secuencia.getAndIncrement());
            ejemplos.add(ejemplo);
            log.debug("Ejemplo creado con id {}", ejemplo.getId());
        } else {
            ejemplos.stream()
                    .filter(e -> e.getId().equals(ejemplo.getId()))
                    .findFirst()
                    .ifPresent(e -> e.setNombre(ejemplo.getNombre()));
            log.debug("Ejemplo actualizado con id {}", ejemplo.getId());
        }
        return ejemplo;
    }

    public List<Ejemplo> listarTodos() {
        return List.copyOf(ejemplos);
    }

}
