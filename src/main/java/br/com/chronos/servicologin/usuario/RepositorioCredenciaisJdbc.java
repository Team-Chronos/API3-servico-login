package br.com.chronos.servicologin.usuario;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class RepositorioCredenciaisJdbc {

    private static final List<String> TABELAS_PRIORIDADE = List.of("usuarios", "usuario", "profissionais", "profissional");
    private static final List<String> COLUNAS_EMAIL = List.of("email");
    private static final List<String> COLUNAS_SENHA = List.of("password_hash", "senha", "password", "senha_hash");
    private static final List<String> COLUNAS_NOME = List.of("nome", "name");
    private static final List<String> COLUNAS_ID = List.of("id", "id_usuario", "id_profissional");
    private static final List<String> COLUNAS_CARGO = List.of("cargo_id", "id_cargo", "cargo");

    private final JdbcTemplate jdbcTemplate;

    public RepositorioCredenciaisJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Usuario> findByEmail(String email) {
        List<MapeamentoCredenciais> mapeamentos = resolverMapeamentos();

        if (mapeamentos.isEmpty()) {
            return Optional.empty();
        }

        for (MapeamentoCredenciais mapeamento : mapeamentos) {
            String sql = "SELECT "
                + mapeamento.expressaoId() + " AS id, "
                + mapeamento.expressaoNome() + " AS nome, "
                + mapeamento.expressaoEmail() + " AS email, "
                + mapeamento.expressaoSenha() + " AS senha, "
                + mapeamento.expressaoCargo() + " AS cargo_id "
                + "FROM " + mapeamento.tabela() + " "
                + "WHERE LOWER(" + mapeamento.colunaEmail() + ") = LOWER(?) LIMIT 1";

            List<Usuario> encontrados = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Usuario(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("senha"),
                    (Integer) rs.getObject("cargo_id")
                ),
                email
            );

            if (!encontrados.isEmpty()) {
                return Optional.of(encontrados.get(0));
            }
        }

        return Optional.empty();
    }

    private List<MapeamentoCredenciais> resolverMapeamentos() {
        String sql = "SELECT table_name, column_name "
                + "FROM information_schema.columns "
                + "WHERE table_schema = DATABASE() "
                + "AND table_name IN ('usuarios','usuario','profissionais','profissional')";

        List<Map<String, Object>> linhas = jdbcTemplate.queryForList(sql);

        if (linhas.isEmpty()) {
            return List.of();
        }

        Map<String, Set<String>> colunasPorTabela = new HashMap<>();

        for (Map<String, Object> linha : linhas) {
            String tabela = String.valueOf(linha.get("table_name")).toLowerCase(Locale.ROOT);
            String coluna = String.valueOf(linha.get("column_name")).toLowerCase(Locale.ROOT);
            colunasPorTabela.computeIfAbsent(tabela, k -> new java.util.HashSet<>()).add(coluna);
        }

        List<String> tabelasOrdenadas = new ArrayList<>(colunasPorTabela.keySet());
        tabelasOrdenadas.sort(Comparator.comparingInt(this::indiceTabela));

        List<MapeamentoCredenciais> mapeamentos = new ArrayList<>();

        for (String tabela : tabelasOrdenadas) {
            Set<String> colunas = colunasPorTabela.get(tabela);
            String colunaEmail = primeiraExistente(colunas, COLUNAS_EMAIL);
            String colunaSenha = primeiraExistente(colunas, COLUNAS_SENHA);

            if (colunaEmail == null || colunaSenha == null) {
                continue;
            }

            String colunaId = primeiraExistente(colunas, COLUNAS_ID);
            String colunaNome = primeiraExistente(colunas, COLUNAS_NOME);
            String colunaCargo = primeiraExistente(colunas, COLUNAS_CARGO);

            String expressaoId = colunaId != null ? colunaId : "0";
            String expressaoNome = colunaNome != null ? colunaNome : "''";
            String expressaoCargo = colunaCargo != null ? colunaCargo : "NULL";

            mapeamentos.add(new MapeamentoCredenciais(
                    tabela,
                    colunaEmail,
                    expressaoId,
                    expressaoNome,
                    colunaEmail,
                    colunaSenha,
                    expressaoCargo
            ));
        }

        return mapeamentos;
    }

    private int indiceTabela(String tabela) {
        int index = TABELAS_PRIORIDADE.indexOf(tabela);
        return index >= 0 ? index : Integer.MAX_VALUE;
    }

    private String primeiraExistente(Set<String> colunas, List<String> opcoes) {
        for (String opcao : opcoes) {
            if (colunas.contains(opcao)) {
                return opcao;
            }
        }

        return null;
    }

    private record MapeamentoCredenciais(
            String tabela,
            String colunaEmail,
            String expressaoId,
            String expressaoNome,
            String expressaoEmail,
            String expressaoSenha,
            String expressaoCargo
    ) {
    }
}
