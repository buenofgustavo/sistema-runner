package runner.assinador.dto;

import java.util.List;

public class AssinaturaResponse {

    private List<CodingDTO> type;
    private String when;
    private String targetFormat;
    private String sigFormat;
    private List<ReferenceDTO> target;
    private String data;

    private ReferenceDTO who;
    private String whoUri;

    private boolean sucesso;
    private String mensagem;

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

    public ReferenceDTO getWho() { return who; }
    public void setWho(ReferenceDTO who) { this.who = who; }

    public String getWhoUri() { return whoUri; }
    public void setWhoUri(String whoUri) { this.whoUri = whoUri; }

    public boolean isSucesso() { return sucesso; }
    public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}