package com.fcv.citas.domain.model;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Agregado de dominio para HU-001/HU-002. Sin dependencias de Spring/JPA/HTTP
 * (ver citas-api/docs/wiki/llm-wiki/wiki/arquitectura.md).
 *
 * El identificador es {@code null} hasta que se persiste: el esquema adoptado
 * (copia exacta de database/reference/db.sql, ver decisiones.md 2026-09-23)
 * usa {@code BIGINT AUTO_INCREMENT} para `users.id`, así que lo asigna la base
 * de datos al guardar, no el dominio. {@code UsuarioRepositoryPort.guardar}
 * siempre devuelve el Usuario con el id ya asignado.
 */
public final class Usuario {

    private final String id;
    private final String nombres;
    private final String apellidos;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String email;
    private final String telefono;
    private final String passwordHash;
    private final Set<RolNombre> roles;
    private final boolean activo;

    private Usuario(String id, String nombres, String apellidos, String tipoDocumento, String numeroDocumento,
                     String email, String telefono, String passwordHash, Set<RolNombre> roles, boolean activo) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.email = email;
        this.telefono = telefono;
        this.passwordHash = passwordHash;
        this.roles = roles;
        this.activo = activo;
    }

    /**
     * RF-01: crea una cuenta USER nueva. Recibe la contraseña ya hasheada
     * (el hashing es responsabilidad de {@code PasswordHasherPort}, no del dominio).
     */
    public static Usuario registrarNuevo(String nombres, String apellidos, String tipoDocumento,
                                          String numeroDocumento, String email, String telefono,
                                          String passwordHash) {
        requerirNoVacio(nombres, "nombres");
        requerirNoVacio(apellidos, "apellidos");
        requerirNoVacio(tipoDocumento, "tipoDocumento");
        requerirNoVacio(numeroDocumento, "numeroDocumento");
        requerirNoVacio(telefono, "telefono");
        requerirEmailValido(email);
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash no puede estar vacío");
        }
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        return new Usuario(null, nombres.trim(), apellidos.trim(), tipoDocumento.trim(), numeroDocumento.trim(),
            emailNormalizado, telefono.trim(), passwordHash, EnumSet.of(RolNombre.USER), true);
    }

    /**
     * RF-07: crea la cuenta de un profesional nuevo (rol PROFESSIONAL en vez de
     * USER). Usada por HU-010 antes de asignarle especialidades/sedes.
     */
    public static Usuario registrarProfesional(String nombres, String apellidos, String tipoDocumento,
                                                String numeroDocumento, String email, String telefono,
                                                String passwordHash) {
        requerirNoVacio(nombres, "nombres");
        requerirNoVacio(apellidos, "apellidos");
        requerirNoVacio(tipoDocumento, "tipoDocumento");
        requerirNoVacio(numeroDocumento, "numeroDocumento");
        requerirNoVacio(telefono, "telefono");
        requerirEmailValido(email);
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash no puede estar vacío");
        }
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        return new Usuario(null, nombres.trim(), apellidos.trim(), tipoDocumento.trim(), numeroDocumento.trim(),
            emailNormalizado, telefono.trim(), passwordHash, EnumSet.of(RolNombre.PROFESSIONAL), true);
    }

    /**
     * Reconstituye un Usuario ya existente desde persistencia (adaptador JPA).
     * A diferencia de {@link #registrarNuevo}, no genera un id nuevo ni fuerza
     * el rol USER por defecto: respeta exactamente lo que hay guardado.
     */
    public static Usuario reconstruir(String id, String nombres, String apellidos, String tipoDocumento,
                                       String numeroDocumento, String email, String telefono,
                                       String passwordHash, Set<RolNombre> roles, boolean activo) {
        return new Usuario(id, nombres, apellidos, tipoDocumento, numeroDocumento, email, telefono,
            passwordHash, roles, activo);
    }

    private static void requerirNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío");
        }
    }

    private static void requerirEmailValido(String email) {
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El email no tiene un formato válido");
        }
    }

    public String getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<RolNombre> getRoles() {
        return roles;
    }

    public boolean isActivo() {
        return activo;
    }
}
