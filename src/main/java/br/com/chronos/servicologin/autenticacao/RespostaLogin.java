package br.com.chronos.servicologin.autenticacao;

public record RespostaLogin(
        String token,
        String tokenType,
        long expiresIn,
        PayloadUsuario user
) {}