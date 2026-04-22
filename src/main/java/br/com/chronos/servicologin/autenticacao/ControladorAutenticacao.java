package br.com.chronos.servicologin.autenticacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class ControladorAutenticacao {

    private final ServicoAutenticacao authService;

    public ControladorAutenticacao(ServicoAutenticacao authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<RespostaLogin> login(@Valid @RequestBody RequisicaoLogin request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}