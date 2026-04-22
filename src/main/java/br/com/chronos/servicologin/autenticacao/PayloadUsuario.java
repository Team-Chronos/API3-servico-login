package br.com.chronos.servicologin.autenticacao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PayloadUsuario(
        Long id,
        String nome,
        String email,
        @JsonProperty("cargo_id")
        Long cargoId
) {}