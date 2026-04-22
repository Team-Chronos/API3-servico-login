package br.com.chronos.servicologin.autenticacao;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import br.com.chronos.servicologin.seguranca.ServicoJwt;
import br.com.chronos.servicologin.usuario.RepositorioCredenciaisJdbc;
import br.com.chronos.servicologin.usuario.Usuario;

@Service
public class ServicoAutenticacao {

    private final RepositorioCredenciaisJdbc credentialsRepository;
    private final ServicoJwt jwtService;

    // Construtor sem PasswordEncoder
    public ServicoAutenticacao(RepositorioCredenciaisJdbc credentialsRepository,
                               ServicoJwt jwtService) {
        this.credentialsRepository = credentialsRepository;
        this.jwtService = jwtService;
    }

    public RespostaLogin login(RequisicaoLogin request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        Usuario user = credentialsRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Email ou senha invalidos"));

        // Comparação direta de texto plano (ou use BCrypt se o hash estiver correto)
        if (!request.senha().equals(user.passwordHash())) {
            throw new BadCredentialsException("Email ou senha invalidos");
        }

        String token = jwtService.generateToken(user);
        PayloadUsuario payload = new PayloadUsuario(user.id(), user.nome(), user.email(), user.cargoId());

        return new RespostaLogin(token, "Bearer", jwtService.getExpirationMs() / 1000, payload);
    }
}