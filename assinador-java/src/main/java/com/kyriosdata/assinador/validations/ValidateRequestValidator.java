package com.kyriosdata.assinador.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyriosdata.assinador.domain.ValidateRequest;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.regex.Pattern;

@Service
public class ValidateRequestValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Regex para validar padrão SemVer (ex. 0.0.1, 1.2.34)
    private static final Pattern SEMVER_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    // Base URI específica da SES-GO
    private static final String SES_GO_BASE_URI = "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca";

    /**
     * Valida rigorosamente os parâmetros de ValidateRequest de acordo com as especificações FHIR SES-GO.
     * 
     * @param request O DTO a ser validado
     * @throws IllegalArgumentException Caso alguma regra de validação seja violada
     */
    public void validate(ValidateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CONFIG.INVALID-PARAMETER: Requisição de validação não pode ser nula");
        }

        // 1. Validar jwsBase64 / signature
        String jwsBase64 = request.getJwsBase64();
        if (jwsBase64 == null || jwsBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("FORMAT.JWS-EMPTY: O parâmetro 'jwsBase64' (ou 'signature') não pode estar vazio");
        }

        // Decodificar Base64 e validar parsing do JWS
        String decodedJwsStr;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(jwsBase64.trim());
            decodedJwsStr = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("FORMAT.BASE64-INVALID: A assinatura enviada não é um Base64 válido");
        }

        validateJwsStructure(decodedJwsStr);

        // 2. Validar referenceTimestamp
        Long timestamp = request.getReferenceTimestamp();
        if (timestamp == null) {
            throw new IllegalArgumentException("CONFIG.TIMESTAMP-EMPTY: O parâmetro 'referenceTimestamp' é obrigatório");
        }
        // Faixa de aceitação definida na spec [1751328000, 4102444800]
        if (timestamp < 1751328000L || timestamp > 4102444800L) {
            throw new IllegalArgumentException("CONFIG.TIMESTAMP-OUT-OF-RANGE: O 'referenceTimestamp' deve estar no intervalo [1751328000, 4102444800]");
        }

        // 3. Validar política de assinatura (signaturePolicy)
        String policy = request.getSignaturePolicy();
        if (policy == null || policy.trim().isEmpty()) {
            throw new IllegalArgumentException("CONFIG.POLICY-EMPTY: O parâmetro 'signaturePolicy' é obrigatório");
        }
        validateSignaturePolicy(policy);

        // 4. Validar configurações operacionais (operationalConfig)
        String configStr = request.getOperationalConfig();
        if (configStr == null || configStr.trim().isEmpty()) {
            throw new IllegalArgumentException("CONFIG.OPERATIONAL-EMPTY: O parâmetro 'operationalConfig' é obrigatório");
        }
        validateJson(configStr, "CONFIG.OPERATIONAL-INVALID", "operationalConfig");

        // 5. Validar bundleJson e provenanceJson (se fornecidos)
        String bundle = request.getBundleJson();
        if (bundle != null && !bundle.trim().isEmpty()) {
            validateJson(bundle, "FORMAT.BUNDLE-INVALID", "bundleJson");
        }

        String provenance = request.getProvenanceJson();
        if (provenance != null && !provenance.trim().isEmpty()) {
            validateJson(provenance, "FORMAT.PROVENANCE-INVALID", "provenanceJson");
        }
    }

    private void validateJson(String json, String errorCode, String fieldName) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException(errorCode + ": O campo '" + fieldName + "' não contém um JSON válido");
        }
    }

    private void validateJwsStructure(String decodedJwsStr) {
        try {
            JsonNode root = objectMapper.readTree(decodedJwsStr);
            
            // JWS JSON Serialization deve possuir 'payload' e 'signatures'
            if (!root.has("payload")) {
                throw new IllegalArgumentException("FORMAT.JWS-PAYLOAD-MISSING: JWS inválido, campo 'payload' obrigatório ausente");
            }
            if (!root.has("signatures") || !root.get("signatures").isArray() || root.get("signatures").isEmpty()) {
                throw new IllegalArgumentException("FORMAT.JWS-SIGNATURES-MISSING: JWS inválido, array 'signatures' obrigatório ausente ou vazio");
            }

            JsonNode sigNode = root.get("signatures").get(0);
            if (!sigNode.has("protected") || !sigNode.has("signature")) {
                throw new IllegalArgumentException("FORMAT.JWS-SIGNATURE-COMPONENTS-MISSING: JWS inválido, componentes 'protected' ou 'signature' ausentes na assinatura");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("FORMAT.JWS-INVALID: A assinatura decodificada não é um JSON JWS válido");
        }
    }

    private void validateSignaturePolicy(String policy) {
        // Formato esperado: {baseUri}|{versão}
        int pipeIdx = policy.lastIndexOf('|');
        if (pipeIdx == -1) {
            throw new IllegalArgumentException("CONFIG.POLICY-INVALID-FORMAT: A política deve estar no formato '{baseUri}|{versão}'");
        }

        String baseUri = policy.substring(0, pipeIdx);
        String version = policy.substring(pipeIdx + 1);

        if (!SES_GO_BASE_URI.equals(baseUri)) {
            throw new IllegalArgumentException("CONFIG.POLICY-URI-INVALID: A URI base da política deve ser exatamente '" + SES_GO_BASE_URI + "'");
        }

        if (!SEMVER_PATTERN.matcher(version).matches()) {
            throw new IllegalArgumentException("CONFIG.POLICY-VERSION-INVALID: A versão da política deve estar no formato SemVer (major.minor.patch), ex: '0.1.2'");
        }
    }
}
