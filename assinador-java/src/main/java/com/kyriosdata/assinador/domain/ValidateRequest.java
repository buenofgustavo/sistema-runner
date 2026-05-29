package com.kyriosdata.assinador.domain;

/**
 * Representa os dados da requisição para operação de validação de assinatura digital.
 * 
 * <p>Esta classe atua como um DTO para transportar informações de validação de assinatura.</p>
 */
public class ValidateRequest {
    
    /**
     * O conteúdo original da assinatura que será validada (opcional para integridade).
     */
    private String content;

    /**
     * A assinatura digital pré-existente (ex. em Base64).
     */
    private String signature;

    /**
     * Token de autenticação, credencial ou PIN opcional.
     */
    private String token;

    // Novos campos da especificação FHIR para validação rigorosa
    private String jwsBase64;
    private String operationalConfig;
    private Long referenceTimestamp;
    private String signaturePolicy;
    private String bundleJson;
    private String provenanceJson;

    public ValidateRequest() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
        if (this.jwsBase64 == null) {
            this.jwsBase64 = signature;
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getJwsBase64() {
        return jwsBase64 != null ? jwsBase64 : signature;
    }

    public void setJwsBase64(String jwsBase64) {
        this.jwsBase64 = jwsBase64;
        if (this.signature == null) {
            this.signature = jwsBase64;
        }
    }

    public String getOperationalConfig() {
        return operationalConfig;
    }

    public void setOperationalConfig(String operationalConfig) {
        this.operationalConfig = operationalConfig;
    }

    public Long getReferenceTimestamp() {
        return referenceTimestamp;
    }

    public void setReferenceTimestamp(Long referenceTimestamp) {
        this.referenceTimestamp = referenceTimestamp;
    }

    public String getSignaturePolicy() {
        return signaturePolicy;
    }

    public void setSignaturePolicy(String signaturePolicy) {
        this.signaturePolicy = signaturePolicy;
    }

    public String getBundleJson() {
        return bundleJson;
    }

    public void setBundleJson(String bundleJson) {
        this.bundleJson = bundleJson;
    }

    public String getProvenanceJson() {
        return provenanceJson;
    }

    public void setProvenanceJson(String provenanceJson) {
        this.provenanceJson = provenanceJson;
    }
}
