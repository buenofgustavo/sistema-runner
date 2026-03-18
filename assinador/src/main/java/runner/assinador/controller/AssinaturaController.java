package runner.assinador.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import runner.assinador.dto.AssinaturaRequest;
import runner.assinador.dto.AssinaturaResponse;
import runner.assinador.service.AssinaturaService;

@RestController
@RequestMapping("/assinaturas")
public class AssinaturaController {

    private final AssinaturaService service;

    public AssinaturaController(AssinaturaService service) {
        this.service = service;
    }

    @PostMapping("/criar")
    public AssinaturaResponse criar(@Valid @RequestBody AssinaturaRequest request) {

        AssinaturaResponse response = new AssinaturaResponse();
        response.setMensagem(service.criar());
        response.setSucesso(true);

        return response;
    }

    @PostMapping("/validar")
    public AssinaturaResponse validar(@Valid @RequestBody AssinaturaRequest request) {

        boolean valido = service.validar(request.getValor());

        AssinaturaResponse response = new AssinaturaResponse();
        response.setSucesso(valido);
        response.setMensagem(valido ? "Assinatura válida" : "Assinatura inválida");

        return response;
    }
}