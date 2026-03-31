package br.com.chronos.servicologin.usuario;

public record Usuario(
        Long id,
        String nome,
        String email,
        String passwordHash,
        Integer cargoId
) {
}
