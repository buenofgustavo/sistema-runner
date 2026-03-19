package runner.assinador.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import runner.assinador.dto.*;
import runner.assinador.service.AssinaturaService;

@RestController
@RequestMapping("/assinaturas")
public class AssinaturaController {

    private final AssinaturaService service;

    public AssinaturaController(AssinaturaService service) {
        this.service = service;
    }

    // 🔹 Criar assinatura (simulação)
    @PostMapping("/criar")
    public AssinaturaResponse criar(
            @Valid @RequestBody CriarAssinaturaRequest request
    ) {
        return service.criar(request);
    }

    // 🔹 Validar assinatura (simulação)
    @PostMapping("/validar")
    public ValidacaoResponse validar(
            @Valid @RequestBody ValidarAssinaturaRequest request
    ) {
        return service.validar(request);
    }
}