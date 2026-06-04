package com.kyriosdata.assinador.web;

import com.kyriosdata.assinador.SignatureService;
import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import com.kyriosdata.assinador.domain.ValidateRequest;
import com.kyriosdata.assinador.validations.SignRequestValidator;
import com.kyriosdata.assinador.validations.ValidateRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AssinadorController {

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private SignRequestValidator signRequestValidator;

    @Autowired
    private ValidateRequestValidator validateRequestValidator;

    @Autowired
    private InactivityShutdownManager shutdownManager;

    @PostMapping("/sign")
    public ResponseEntity<SignatureResponse> sign(@RequestBody SignRequest request) {
        shutdownManager.resetTimer();
        
        // Validar rigorosamente conforme a especificação apenas se for fluxo avançado FHIR
        if (request.getBundleJson() != null && !request.getBundleJson().isEmpty()) {
            signRequestValidator.validate(request);
        }
        
        // Executar a assinatura (simulação)
        SignatureResponse response = signatureService.sign(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<SignatureResponse> validate(@RequestBody ValidateRequest request) {
        shutdownManager.resetTimer();
        
        // Validar rigorosamente conforme a especificação apenas se for fluxo avançado FHIR
        if (request.getContent() == null || request.getContent().isEmpty()) {
            validateRequestValidator.validate(request);
        }
        
        // Executar a validação (simulação)
        SignatureResponse response = signatureService.validate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        shutdownManager.resetTimer();
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Assinador rodando com sucesso");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Map<String, String>> shutdown() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "SHUTTING_DOWN");
        response.put("message", "Encerrando servidor do Assinador...");
        
        // Iniciar desligamento em segundo plano para permitir o retorno da resposta HTTP
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
            shutdownManager.shutdown();
        }).start();

        return ResponseEntity.ok(response);
    }
}
