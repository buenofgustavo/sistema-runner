package runner.assinador.service;

import org.springframework.stereotype.Service;

@Service
public class AssinaturaService {

    private static final String ASSINATURA_FIXA = "ASSINATURA_FAKE";

    // Simula criação de assinatura
    public String criar() {
        return ASSINATURA_FIXA;
    }

    // Simula validação de assinatura
    public boolean validar(String assinatura) {
        return ASSINATURA_FIXA.equals(assinatura);
    }
}