package runner.assinador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ValidarAssinaturaRequest {

    @NotEmpty
    private List<CodingDTO> type;

    @NotBlank
    private String when;

    private String targetFormat;

    @NotBlank
    private String sigFormat;

    @NotEmpty
    private List<ReferenceDTO> target;

    @NotBlank
    private String data;

    @NotBlank
    private String signature; // assinatura a validar

    private ReferenceDTO who;

    private String whoUri;

    // getters e setters
    public List<CodingDTO> getType() { return type; }
    public void setType(List<CodingDTO> type) { this.type = type; }

    public String getWhen() { return when; }
    public void setWhen(String when) { this.when = when; }

    public String getTargetFormat() { return targetFormat; }
    public void setTargetFormat(String targetFormat) { this.targetFormat = targetFormat; }

    public String getSigFormat() { return sigFormat; }
    public void setSigFormat(String sigFormat) { this.sigFormat = sigFormat; }

    public List<ReferenceDTO> getTarget() { return target; }
    public void setTarget(List<ReferenceDTO> target) { this.target = target; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public ReferenceDTO getWho() { return who; }
    public void setWho(ReferenceDTO who) { this.who = who; }

    public String getWhoUri() { return whoUri; }
    public void setWhoUri(String whoUri) { this.whoUri = whoUri; }
}