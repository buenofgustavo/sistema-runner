package com.kyriosdata.assinador.domain;

import java.util.List;

/**
 * DTO que representa a entrada para o caso de uso "Criar assinatura digital"
 * conforme o guia FHIR SES-GO.
 *
 * Campos principais incorporados:
 * - bundleJson: instância do Bundle (serializada JSON)
 * - provenanceJson: instância do Provenance (serializada JSON)
 * - material: material criptográfico (PEM/PKCS12/SMARTCARD/TOKEN/REMOTE) como
 * JSON/string
 * - certChain: cadeia de certificados (array de strings base64)
 * - referenceTimestamp: timestamp de referência (NumericDate, segundos)
 * - strategy: 'iat' ou 'tsa'
 * - signaturePolicy: URI da política de assinatura (sigPId)
 * - operationalConfig: configurações operacionais (JSON/string)
 * - token: credencial/PIN opcional (por ex. para smartcard/token)
 */
public class SignRequest {

    // Bundle FHIR (JSON)
    private String bundleJson;

    // Provenance FHIR (JSON)
    private String provenanceJson;

    // Material criptográfico: formato livre (pode ser JSON com tipo/fields ou
    // string)
    private String material;

    // Cadeia de certificados em base64 (primeiro elemento = certificado do
    // signatário)
    private List<String> certChain;

    // Timestamp de referência (segundos desde epoch)
    private Long referenceTimestamp;

    // Estratégia de timestamp: 'iat' (default) ou 'tsa'
    private String strategy;

    // URI da política de assinatura (sigPId)
    private String signaturePolicy;

    // Configurações operacionais (JSON/string)
    private String operationalConfig;

    // Token/credencial/PIN opcional
    private String token;

    public SignRequest() {
    }

    public String getBundleJson() {
        return bundleJson;
    }

    public void setBundleJson(String bundleJson) {
        this.bundleJson = bundleJson;
    }

    /**
     * Backwards-compatible accessor used by older code paths that treated
     * `content` as the bundle JSON string.
     */
    public String getContent() {
        return this.bundleJson;
    }

    /**
     * Backwards-compatible mutator used by older code paths that pass the
     * bundle JSON as `content`.
     */
    public void setContent(String content) {
        this.bundleJson = content;
    }

    public String getProvenanceJson() {
        return provenanceJson;
    }

    public void setProvenanceJson(String provenanceJson) {
        this.provenanceJson = provenanceJson;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public List<String> getCertChain() {
        return certChain;
    }

    public void setCertChain(List<String> certChain) {
        this.certChain = certChain;
    }

    public Long getReferenceTimestamp() {
        return referenceTimestamp;
    }

    public void setReferenceTimestamp(Long referenceTimestamp) {
        this.referenceTimestamp = referenceTimestamp;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getSignaturePolicy() {
        return signaturePolicy;
    }

    public void setSignaturePolicy(String signaturePolicy) {
        this.signaturePolicy = signaturePolicy;
    }

    public String getOperationalConfig() {
        return operationalConfig;
    }

    public void setOperationalConfig(String operationalConfig) {
        this.operationalConfig = operationalConfig;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
