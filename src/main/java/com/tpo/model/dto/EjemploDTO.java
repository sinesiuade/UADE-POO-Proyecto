package com.tpo.model.dto;

import com.tpo.model.entities.Ejemplo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EjemploDTO {

    private Long id;
    private String nombre;

    public static EjemploDTO fromEntity(Ejemplo entity) {
        if (entity == null) {
            return null;
        }
        return new EjemploDTO(entity.getId(), entity.getNombre());
    }

    public Ejemplo toEntity() {
        return new Ejemplo(id, nombre);
    }
}
