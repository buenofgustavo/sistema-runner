package runner.assinador.dto;

import jakarta.validation.constraints.NotBlank;

public class ReferenceDTO {

    @NotBlank
    private String reference; // ex: Practitioner/123

    private String display;

    // getters e setters
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }
}