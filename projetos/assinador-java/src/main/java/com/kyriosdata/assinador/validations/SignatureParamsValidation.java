package com.kyriosdata.assinador.validations;

import com.kyriosdata.assinador.enums.Operations;
import org.springframework.stereotype.Service;

/**
 * Validador de parâmetros para operações de assinatura.
 */
@Service
public class SignatureParamsValidation {

    /**
     * Valida os argumentos e determina qual operação executar.
     * 
     * @param args argumentos da linha de comando
     * @return a operação a ser executada
     */
    public Operations signatureParams(String[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        String operation = args[0].toLowerCase();
        if ("sign".equals(operation)) {
            return Operations.SIGN;
        } else if ("validate".equals(operation)) {
            return Operations.VALIDATE;
        }

        return null;
    }

    /**
     * Valida os parâmetros para operação de assinatura (SIGN).
     * 
     * @param args argumentos da linha de comando
     * @return true se os parâmetros são válidos, false caso contrário
     */
    public boolean createSignatureParams(String[] args) {
        // Validar se existem parâmetros suficientes
        if (args == null || args.length < 2) {
            System.err.println("Erro: parâmetros insuficientes para operação SIGN");
            return false;
        }

        // Validar se o conteúdo não está vazio
        String content = args[1];
        if (content == null || content.trim().isEmpty()) {
            System.err.println("Erro: conteúdo a assinar não pode estar vazio");
            return false;
        }

        return true;
    }

    /**
     * Valida os parâmetros para operação de validação (VALIDATE).
     * 
     * @param args argumentos da linha de comando
     * @return true se os parâmetros são válidos, false caso contrário
     */
    public boolean validateSignatureParams(String[] args) {
        // Validar se existem parâmetros suficientes
        if (args == null || args.length < 3) {
            System.err.println("Erro: parâmetros insuficientes para operação VALIDATE");
            return false;
        }

        // Validar se o conteúdo não está vazio
        String content = args[1];
        if (content == null || content.trim().isEmpty()) {
            System.err.println("Erro: conteúdo a validar não pode estar vazio");
            return false;
        }

        // Validar se a assinatura não está vazia
        String signature = args[2];
        if (signature == null || signature.trim().isEmpty()) {
            System.err.println("Erro: assinatura não pode estar vazia");
            return false;
        }

        return true;
    }
}
