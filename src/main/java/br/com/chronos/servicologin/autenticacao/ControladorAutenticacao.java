package br.com.chronos.servicologin.autenticacao;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ControladorAutenticacao {

    private final ServicoAutenticacao authService;

    public ControladorAutenticacao(ServicoAutenticacao authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<RespostaLogin> login(@Valid @RequestBody RequisicaoLogin request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<PayloadUsuario> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "api3-servico-login"));
    }
}
