package runner.assinador.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidarAssinaturaRequest {

    @NotBlank
    private String payload;

    @NotBlank
    private String assinatura;

    @NotBlank
    private String algoritmo;

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getAssinatura() { return assinatura; }
    public void setAssinatura(String assinatura) { this.assinatura = assinatura; }

    public String getAlgoritmo() { return algoritmo; }
    public void setAlgoritmo(String algoritmo) { this.algoritmo = algoritmo; }
}