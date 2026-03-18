package runner.assinador.service;

import org.springframework.stereotype.Service;

@Service
public class AssinaturaService {

    private static final String ASSINATURA_FIXA = "ASSINATURA_FAKE";

    // 🔹 Simula criação de assinatura
    public String criar(String payload, String algoritmo, String certificado) {

        // Aqui você poderia validar regras básicas se quiser
        // Exemplo: algoritmo suportado
        if (!"SHA256".equalsIgnoreCase(algoritmo)) {
            throw new IllegalArgumentException("Algoritmo não suportado");
        }

        // Simulação: sempre retorna a mesma assinatura
        return ASSINATURA_FIXA;
    }

    // 🔹 Simula validação de assinatura
    public boolean validar(String payload, String assinatura, String algoritmo) {

        if (!"SHA256".equalsIgnoreCase(algoritmo)) {
            return false;
        }

        // Simulação: compara com assinatura fixa
        return ASSINATURA_FIXA.equals(assinatura);
    }
}