package com.fcv.citas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void registrarNuevo_asignaRolUserPorDefecto() {
        Usuario usuario = Usuario.registrarNuevo(
            "Ana", "Pérez", "CC", "1000000001", "Ana.Perez@Example.com", "3000000000", "hash-simulado"
        );

        assertThat(usuario.getRoles()).containsExactly(RolNombre.USER);
        assertThat(usuario.isActivo()).isTrue();
        // El id lo asigna la base de datos (BIGINT AUTO_INCREMENT) al guardar, no el dominio.
        assertThat(usuario.getId()).isNull();
    }

    @Test
    void registrarNuevo_normalizaElEmailAMinusculas() {
        Usuario usuario = Usuario.registrarNuevo(
            "Ana", "Pérez", "CC", "1000000001", "Ana.Perez@Example.com", "3000000000", "hash-simulado"
        );

        assertThat(usuario.getEmail()).isEqualTo("ana.perez@example.com");
    }

    @Test
    void registrarNuevo_rechazaNombresVacios() {
        assertThatThrownBy(() -> Usuario.registrarNuevo(
            "  ", "Pérez", "CC", "1000000001", "ana@example.com", "3000000000", "hash-simulado"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registrarNuevo_rechazaEmailConFormatoInvalido() {
        assertThatThrownBy(() -> Usuario.registrarNuevo(
            "Ana", "Pérez", "CC", "1000000001", "no-es-un-email", "3000000000", "hash-simulado"
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
