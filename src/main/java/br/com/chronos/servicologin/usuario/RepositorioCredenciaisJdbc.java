package br.com.chronos.servicologin.usuario;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioCredenciaisJdbc {

    private final JdbcTemplate jdbcTemplate;

    public RepositorioCredenciaisJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT id, nome, email, senha_hash, cargo_id FROM usuario WHERE LOWER(email) = LOWER(?) LIMIT 1";
        try {
            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    return Optional.of(new Usuario(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("senha_hash"),
                        rs.getLong("cargo_id")
                    ));
                }
                return Optional.empty();
            }, email);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}