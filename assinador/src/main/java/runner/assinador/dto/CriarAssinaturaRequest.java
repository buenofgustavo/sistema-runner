package runner.assinador.dto;

import jakarta.validation.constraints.NotBlank;

public class CriarAssinaturaRequest {

    @NotBlank
    private String payload; // conteúdo a ser assinado

    @NotBlank
    private String algoritmo; // ex: SHA256

    @NotBlank
    private String certificado; // simulado

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getAlgoritmo() { return algoritmo; }
    public void setAlgoritmo(String algoritmo) { this.algoritmo = algoritmo; }

    public String getCertificado() { return certificado; }
    public void setCertificado(String certificado) { this.certificado = certificado; }
}