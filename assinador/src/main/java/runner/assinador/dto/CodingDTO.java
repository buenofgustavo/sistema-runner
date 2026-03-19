package runner.assinador.dto;

import jakarta.validation.constraints.NotBlank;

public class CodingDTO {

    @NotBlank
    private String system;

    @NotBlank
    private String code;

    private String display;

    // getters e setters
    public String getSystem() { return system; }
    public void setSystem(String system) { this.system = system; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }
}