package br.com.chronos.servicologin.autenticacao;

import br.com.chronos.servicologin.seguranca.ServicoJwt;
import br.com.chronos.servicologin.usuario.RepositorioCredenciaisJdbc;
import br.com.chronos.servicologin.usuario.RepositorioUsuario;
import br.com.chronos.servicologin.usuario.Usuario;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ServicoAutenticacao {

    private final RepositorioCredenciaisJdbc credentialsRepository;
    private final RepositorioUsuario userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicoJwt jwtService;

    public ServicoAutenticacao(RepositorioCredenciaisJdbc credentialsRepository,
                       RepositorioUsuario userRepository,
                       PasswordEncoder passwordEncoder,
                       ServicoJwt jwtService) {
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RespostaLogin login(RequisicaoLogin request) {
        String normalizedEmail = request.email() == null ? "" : request.email().trim().toLowerCase();

        Usuario user = buscarUsuarioPorEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Email ou senha invalidos"));

        if (!senhaValida(request.senha(), user.passwordHash())) {
            throw new BadCredentialsException("Email ou senha invalidos");
        }

        String token = jwtService.generateToken(user);
        PayloadUsuario payload = new PayloadUsuario(user.id(), user.nome(), user.email(), user.cargoId());

        return new RespostaLogin(token, "Bearer", jwtService.getExpirationMs() / 1000, payload);
    }

    public PayloadUsuario me(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        Usuario user = buscarUsuarioPorEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Usuario nao encontrado"));

        return new PayloadUsuario(user.id(), user.nome(), user.email(), user.cargoId());
    }

    private java.util.Optional<Usuario> buscarUsuarioPorEmail(String email) {
        java.util.Optional<Usuario> usuarioViaJdbc = credentialsRepository.findByEmail(email);

        if (usuarioViaJdbc.isPresent()) {
            return usuarioViaJdbc;
        }

        return userRepository.findByEmailIgnoreCase(email)
                .map(u -> new Usuario(u.getId(), u.getNome(), u.getEmail(), u.getPasswordHash(), u.getCargoId()));
    }

    private boolean senhaValida(String senhaInformada, String senhaArmazenada) {
        if (senhaArmazenada == null || senhaInformada == null) {
            return false;
        }

        if (senhaArmazenada.startsWith("$2a$")
                || senhaArmazenada.startsWith("$2b$")
                || senhaArmazenada.startsWith("$2y$")) {
            return passwordEncoder.matches(senhaInformada, senhaArmazenada);
        }

        return senhaInformada.equals(senhaArmazenada);
    }
}
