package runner.assinador.service;

import org.springframework.stereotype.Service;
import runner.assinador.dto.*;

import java.time.Instant;

@Service
public class AssinaturaService {

    private static final String ASSINATURA_FIXA = "ASSINATURA_FAKE";

    // 🔹 Criar assinatura (simulação)
    public AssinaturaResponse criar(CriarAssinaturaRequest request) {

        validarRegrasBasicas(request);

        AssinaturaResponse response = new AssinaturaResponse();

        // Copia os dados (simula retorno FHIR)
        response.setType(request.getType());
        response.setWhen(request.getWhen());
        response.setTargetFormat(request.getTargetFormat());
        response.setSigFormat(request.getSigFormat());
        response.setTarget(request.getTarget());

        // Aqui está a "assinatura simulada"
        response.setData(ASSINATURA_FIXA);

        response.setWho(request.getWho());
        response.setWhoUri(request.getWhoUri());

        response.setSucesso(true);
        response.setMensagem("Assinatura criada com sucesso");

        return response;
    }

    // 🔹 Validar assinatura (simulação)
    public ValidacaoResponse validar(ValidarAssinaturaRequest request) {

        validarRegrasBasicas(request);

        ValidacaoResponse response = new ValidacaoResponse();

        boolean valido = ASSINATURA_FIXA.equals(request.getSignature());

        response.setSucesso(valido);
        response.setMensagem(
                valido ? "Assinatura válida" : "Assinatura inválida"
        );

        return response;
    }

    // 🔹 Validações básicas (comuns)
    private void validarRegrasBasicas(CriarAssinaturaRequest request) {

        validarWhen(request.getWhen());
        validarSigFormat(request.getSigFormat());
        validarType(request.getType());
    }

    private void validarRegrasBasicas(ValidarAssinaturaRequest request) {

        validarWhen(request.getWhen());
        validarSigFormat(request.getSigFormat());
        validarType(request.getType());
    }

    // 🔹 Valida timestamp ISO
    private void validarWhen(String when) {
        try {
            Instant.parse(when);
        } catch (Exception e) {
            throw new IllegalArgumentException("Campo 'when' inválido (use ISO 8601)");
        }
    }

    // 🔹 Valida formato da assinatura
    private void validarSigFormat(String sigFormat) {
        if (!sigFormat.equalsIgnoreCase("application/jose")) {
            throw new IllegalArgumentException("sigFormat inválido. Use application/jose");
        }
    }

    // 🔹 Valida type (mínimo 1)
    private void validarType(java.util.List<CodingDTO> type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("type deve conter ao menos um elemento");
        }
    }
}