package runner.assinador.dto;

import jakarta.validation.constraints.NotBlank;

public class AssinaturaRequest {

    @NotBlank
    private String documento;

    @NotBlank
    private String valor; // pode representar conteúdo ou assinatura

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}