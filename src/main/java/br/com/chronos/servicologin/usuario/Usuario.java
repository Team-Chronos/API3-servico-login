package br.com.chronos.servicologin.usuario;

public record Usuario(
        Integer id,
        String nome,
        String email,
        String passwordHash,
        Long cargoId
) {
}
