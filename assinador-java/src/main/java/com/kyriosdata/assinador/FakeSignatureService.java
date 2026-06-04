package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.ValidateRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import org.springframework.stereotype.Service;

/**
 * Implementação simulada do serviço de assinatura digital.
 * 
 * <p>Esta é uma implementação fake para testes e desenvolvimento.</p>
 */
@Service
public class FakeSignatureService implements SignatureService {

    private static final String FAKE_SIGNATURE = "MOCKED_SIGNATURE_BASE64_==";

    @Override
    public SignatureResponse sign(SignRequest request) {
        if (request == null) {
            return new SignatureResponse(null, false, "Requisição inválida");
        }

        // Se contiver bundleJson, estamos no fluxo de assinatura avançada FHIR
        if (request.getBundleJson() != null && !request.getBundleJson().isEmpty()) {
            String jwsBase64 = generateSimulatedJws(request);
            return new SignatureResponse(jwsBase64, true, "Assinatura digital simulada criada com sucesso (JWS)");
        }

        // Fluxo CLI legível original
        if (request.getContent() == null || request.getContent().isEmpty()) {
            return new SignatureResponse(null, false, "Parâmetro 'content' inválido ou ausente");
        }
        return new SignatureResponse(FAKE_SIGNATURE, true, "Assinatura criada com sucesso");
    }

    @Override
    public SignatureResponse validate(ValidateRequest request) {
        if (request == null) {
            return new SignatureResponse(null, false, "Requisição inválida");
        }

        // Se contiver content, tratamos como o fluxo CLI legível original
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            if (request.getSignature() == null || request.getSignature().isEmpty()) {
                return new SignatureResponse(null, false, "Parâmetro 'signature' inválido ou ausente");
            }
            boolean isValid = FAKE_SIGNATURE.equals(request.getSignature());
            return new SignatureResponse(request.getSignature(), isValid, isValid ? "Assinatura é válida" : "Assinatura é inválida");
        }

        // Se for validação avançada FHIR
        if (request.getJwsBase64() != null && !request.getJwsBase64().isEmpty()) {
            // Em simulação, consideramos válidos JWS estruturalmente corretos
            return new SignatureResponse(request.getJwsBase64(), true, "Assinatura é válida (simulada)");
        }

        return new SignatureResponse(null, false, "Parâmetro 'content' ou 'jwsBase64' inválido ou ausente");
    }

    private String generateSimulatedJws(SignRequest request) {
        String x5cStr = "";
        if (request.getCertChain() != null && !request.getCertChain().isEmpty()) {
            x5cStr = "\"" + request.getCertChain().get(0) + "\"";
        } else {
            x5cStr = "\"MOCKED_CERTIFICATE_BASE64\"";
        }
        
        String protectedHeader = "{\"alg\":\"RS256\",\"x5c\":[" + x5cStr + "],\"sigPId\":\"" + request.getSignaturePolicy() + "\"}";
        String protectedHeaderBase64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(protectedHeader.getBytes());
        
        String payload = "{\"bundleHash\":\"simulated_hash_12345\"}";
        String payloadBase64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());
        
        String signatureValue = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("SIMULATED_CRYPTOGRAPHIC_SIGNATURE_VALUE".getBytes());
        
        String jwsJson = "{"
            + "\"payload\":\"" + payloadBase64 + "\","
            + "\"signatures\":[{"
            + "\"protected\":\"" + protectedHeaderBase64 + "\","
            + "\"signature\":\"" + signatureValue + "\""
            + "}]"
            + "}";
            
        return java.util.Base64.getEncoder().encodeToString(jwsJson.getBytes());
    }
}
