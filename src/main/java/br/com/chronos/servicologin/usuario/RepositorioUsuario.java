package br.com.chronos.servicologin.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioUsuario extends JpaRepository<UsuarioEntidade, Long> {

    Optional<UsuarioEntidade> findByEmailIgnoreCase(String email);
}
