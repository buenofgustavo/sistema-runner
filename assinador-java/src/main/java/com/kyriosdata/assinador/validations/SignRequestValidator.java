package com.kyriosdata.assinador.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyriosdata.assinador.domain.SignRequest;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SignRequestValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Regex para validar padrão SemVer (ex. 0.0.1, 1.2.34)
    private static final Pattern SEMVER_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    // Base URI específica da SES-GO
    private static final String SES_GO_BASE_URI = "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca";

    /**
     * Valida rigorosamente os parâmetros de SignRequest de acordo com as especificações FHIR SES-GO.
     * 
     * @param request O DTO a ser validado
     * @throws IllegalArgumentException Caso alguma regra de validação seja violada
     */
    public void validate(SignRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CONFIG.INVALID-PARAMETER: Requisição não pode ser nula");
        }

        // 1. Validar bundleJson
        String bundle = request.getBundleJson();
        if (bundle == null || bundle.trim().isEmpty()) {
            throw new IllegalArgumentException("FORMAT.BUNDLE-EMPTY: O parâmetro 'bundleJson' não pode estar vazio");
        }
        validateJson(bundle, "FORMAT.BUNDLE-INVALID", "bundleJson");

        // 2. Validar provenanceJson
        String provenance = request.getProvenanceJson();
        if (provenance == null || provenance.trim().isEmpty()) {
            throw new IllegalArgumentException("FORMAT.PROVENANCE-EMPTY: O parâmetro 'provenanceJson' não pode estar vazio");
        }
        validateJson(provenance, "FORMAT.PROVENANCE-INVALID", "provenanceJson");

        // 3. Validar material criptográfico
        String materialStr = request.getMaterial();
        if (materialStr == null || materialStr.trim().isEmpty()) {
            throw new IllegalArgumentException("CONFIG.MATERIAL-EMPTY: O parâmetro 'material' não pode estar vazio");
        }
        validateMaterial(materialStr);

        // 4. Validar cadeia de certificados (certChain)
        List<String> certChain = request.getCertChain();
        if (certChain == null || certChain.isEmpty()) {
            throw new IllegalArgumentException("CERT.CHAIN-EMPTY: A cadeia de certificados 'certChain' não pode estar vazia");
        }
        for (int i = 0; i < certChain.size(); i++) {
            String cert = certChain.get(i);
            if (cert == null || cert.trim().isEmpty()) {
                throw new IllegalArgumentException("FORMAT.BASE64-INVALID: Elemento da cadeia de certificados no índice " + i + " está vazio");
            }
            if (!isValidBase64(cert)) {
                throw new IllegalArgumentException("FORMAT.BASE64-INVALID: Certificado no índice " + i + " não é um Base64 válido");
            }
        }

        // 5. Validar referenceTimestamp
        Long timestamp = request.getReferenceTimestamp();
        if (timestamp == null) {
            throw new IllegalArgumentException("CONFIG.TIMESTAMP-EMPTY: O parâmetro 'referenceTimestamp' é obrigatório");
        }
        // Faixa de aceitação definida na spec [1751328000, 4102444800] (01/07/2025 a 31/12/2099)
        if (timestamp < 1751328000L || timestamp > 4102444800L) {
            throw new IllegalArgumentException("CONFIG.TIMESTAMP-OUT-OF-RANGE: O 'referenceTimestamp' deve estar no intervalo [1751328000, 4102444800]");
        }

        // 6. Validar strategy
        String strategy = request.getStrategy();
        if (strategy == null || strategy.trim().isEmpty()) {
            throw new IllegalArgumentException("CONFIG.STRATEGY-EMPTY: A estratégia 'strategy' não pode estar vazia");
        }
        if (!"iat".equalsIgnoreCase(strategy) && !"tsa".equalsIgnoreCase(strategy)) {
            throw new IllegalArgumentException("CONFIG.STRATEGY-INVALID: Estratégia inválida. Valores permitidos: 'iat', 'tsa'");
        }

        // 7. Validar política de assinatura (signaturePolicy)
        String policy = request.getSignaturePolicy();
        if (policy == null || policy.trim().isEmpty()) {
            throw new IllegalArgumentException("CONFIG.POLICY-EMPTY: O parâmetro 'signaturePolicy' é obrigatório");
        }
        validateSignaturePolicy(policy);

        // 8. Validar configurações operacionais (operationalConfig)
        String configStr = request.getOperationalConfig();
        if (configStr == null || configStr.trim().isEmpty()) {
            throw new IllegalArgumentException("CONFIG.OPERATIONAL-EMPTY: O parâmetro 'operationalConfig' é obrigatório");
        }
        validateJson(configStr, "CONFIG.OPERATIONAL-INVALID", "operationalConfig");
    }

    private void validateJson(String json, String errorCode, String fieldName) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException(errorCode + ": O campo '" + fieldName + "' não contém um JSON válido");
        }
    }

    private void validateMaterial(String materialStr) {
        try {
            JsonNode root = objectMapper.readTree(materialStr);
            if (!root.has("type")) {
                throw new IllegalArgumentException("CONFIG.MATERIAL-TYPE-MISSING: O material criptográfico deve conter um campo 'type'");
            }
            String type = root.get("type").asText().toUpperCase();
            switch (type) {
                case "PEM":
                    // Deve possuir o campo 'key' ou 'content' no formato PEM
                    if (!root.has("key") && !root.has("content")) {
                        throw new IllegalArgumentException("CONFIG.PEM-KEY-MISSING: Material tipo PEM requer a chave privada no campo 'key' ou 'content'");
                    }
                    break;
                case "PKCS12":
                    // Deve possuir conteudo codificado em base64, senha e alias
                    if (!root.has("content") || !root.has("password") || !root.has("alias")) {
                        throw new IllegalArgumentException("CONFIG.PKCS12-PARAMETERS-MISSING: Material tipo PKCS12 requer 'content' (base64), 'password' e 'alias'");
                    }
                    String p12Content = root.get("content").asText();
                    if (!isValidBase64(p12Content)) {
                        throw new IllegalArgumentException("FORMAT.BASE64-INVALID: O campo 'content' do material PKCS12 deve ser um Base64 válido");
                    }
                    break;
                case "SMARTCARD":
                case "TOKEN":
                    // Requer PIN e identificador
                    if (!root.has("pin") || !root.has("identifier")) {
                        throw new IllegalArgumentException("CONFIG.PKCS11-PARAMETERS-MISSING: Material tipo " + type + " requer 'pin' e 'identifier' da chave");
                    }
                    break;
                case "REMOTE":
                    // Requer endereco do servico remoto
                    if (!root.has("address") || !root.has("credential")) {
                        throw new IllegalArgumentException("CONFIG.REMOTE-PARAMETERS-MISSING: Material tipo REMOTE requer 'address' e 'credential'");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("CONFIG.MATERIAL-TYPE-INVALID: Tipo de material criptográfico não suportado: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("CONFIG.MATERIAL-INVALID: Parâmetro 'material' não pôde ser analisado como JSON");
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

    private boolean isValidBase64(String str) {
        if (str == null) return false;
        try {
            Base64.getDecoder().decode(str.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
