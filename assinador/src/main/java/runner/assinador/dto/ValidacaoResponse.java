package runner.assinador.dto;

public class ValidacaoResponse {

    private boolean sucesso;
    private String mensagem;

    // getters e setters
    public boolean isSucesso() { return sucesso; }
    public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}