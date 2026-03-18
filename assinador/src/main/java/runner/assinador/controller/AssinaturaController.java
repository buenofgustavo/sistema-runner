package runner.assinador.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import runner.assinador.dto.CriarAssinaturaRequest;
import runner.assinador.dto.ValidarAssinaturaRequest;
import runner.assinador.dto.AssinaturaResponse;
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
    public AssinaturaResponse criar(@Valid @RequestBody CriarAssinaturaRequest request) {

        String assinatura = service.criar(
                request.getPayload(),
                request.getAlgoritmo(),
                request.getCertificado()
        );

        AssinaturaResponse response = new AssinaturaResponse();
        response.setAssinatura(assinatura);
        response.setSucesso(true);
        response.setMensagem("Assinatura criada com sucesso");

        return response;
    }

    // 🔹 Validar assinatura (simulação)
    @PostMapping("/validar")
    public AssinaturaResponse validar(@Valid @RequestBody ValidarAssinaturaRequest request) {

        boolean valida = service.validar(
                request.getPayload(),
                request.getAssinatura(),
                request.getAlgoritmo()
        );

        AssinaturaResponse response = new AssinaturaResponse();
        response.setSucesso(valida);
        response.setAssinatura(request.getAssinatura());
        response.setMensagem(
                valida ? "Assinatura válida" : "Assinatura inválida"
        );

        return response;
    }
}