package com.kyriosdata.assinador;

import com.kyriosdata.assinador.enums.Operations;
import com.kyriosdata.assinador.validations.SignatureParamsValidation;
import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.ValidateRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicação principal para operações de assinatura digital.
 * 
 * <p>Implementa CommandLineRunner para processar argumentos da linha de comando.</p>
 */
@SpringBootApplication
public class AssinadorApplication implements CommandLineRunner {

    @Autowired
    private SignatureService engine;

    @Autowired
    private SignatureParamsValidation paramsValidation;

    public static void main(String[] args) {
        SpringApplication.run(AssinadorApplication.class, args);
    }

    @Override
    public void run(String @NonNull ... args) {
        String result = "";

        Operations op = paramsValidation.signatureParams(args);

        switch (op) {
            case SIGN -> {
                if (paramsValidation.createSignatureParams(args)) {
                    result = processSign(args);
                }
            }
            case VALIDATE -> {
                if (paramsValidation.validateSignatureParams(args)) {
                    result = processValidate(args);
                }
            }
            case null -> {
                result = "Erro: operação inválida. Use 'sign' ou 'validate'";
            }
        }

        System.out.print(result);
    }

    /**
     * Processa a operação de assinatura.
     * 
     * @param args argumentos contendo o conteúdo a assinar
     * @return resultado da assinatura
     */
    private String processSign(String[] args) {
        SignRequest request = new SignRequest();
        request.setContent(args[1]);
        if (args.length > 2) {
            request.setToken(args[2]);
        }

        SignatureResponse response = engine.sign(request);
        return formatResponse(response);
    }

    /**
     * Processa a operação de validação.
     * 
     * @param args argumentos contendo o conteúdo e a assinatura
     * @return resultado da validação
     */
    private String processValidate(String[] args) {
        ValidateRequest request = new ValidateRequest();
        request.setContent(args[1]);
        request.setSignature(args[2]);
        if (args.length > 3) {
            request.setToken(args[3]);
        }

        SignatureResponse response = engine.validate(request);
        return formatResponse(response);
    }

    /**
     * Formata a resposta para exibição.
     * 
     * @param response a resposta da operação
     * @return string formatada com o resultado
     */
    private String formatResponse(SignatureResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(response.isValid() ? "Sucesso" : "Falha").append("\n");
        sb.append("Mensagem: ").append(response.getMessage()).append("\n");
        if (response.getSignature() != null) {
            sb.append("Assinatura: ").append(response.getSignature()).append("\n");
        }
        return sb.toString();
    }
}
