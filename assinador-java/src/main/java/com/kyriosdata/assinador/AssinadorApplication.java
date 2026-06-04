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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    @Override
    public void run(String @NonNull ... args) {
        boolean isServer = false;
        for (String arg : args) {
            if ("--server".equals(arg)) {
                isServer = true;
                break;
            }
        }

        if (isServer) {
            // Em modo servidor, os endpoints REST gerenciam as requisições
            return;
        }

        if (args == null || args.length == 0) {
            System.out.println("Assinador Java - Modos de uso:\n" +
                               "  Servidor: java -jar assinador.jar --server [--port <porta>]\n" +
                               "  Local:    java -jar assinador.jar <sign|validate> --input <arquivo> ...");
            return;
        }

        Operations op = paramsValidation.signatureParams(args);
        if (op == null) {
            System.err.println("Erro: operação inválida. Use 'sign' ou 'validate'");
            System.exit(2);
        }

        boolean hasFlags = false;
        for (String arg : args) {
            if ("--input".equals(arg) || "--output".equals(arg) || "--signature".equals(arg)) {
                hasFlags = true;
                break;
            }
        }

        if (op == Operations.SIGN) {
            String inputPath = null;
            String outputPath = null;
            String token = null;

            if (hasFlags) {
                for (int i = 1; i < args.length; i++) {
                    if ("--input".equals(args[i]) && i + 1 < args.length) {
                        inputPath = args[i + 1];
                        i++;
                    } else if ("--output".equals(args[i]) && i + 1 < args.length) {
                        outputPath = args[i + 1];
                        i++;
                    } else if ("--token".equals(args[i]) && i + 1 < args.length) {
                        token = args[i + 1];
                        i++;
                    }
                }

                if (inputPath == null || inputPath.trim().isEmpty()) {
                    System.err.println("Erro: parâmetro --input é obrigatório");
                    System.exit(2);
                }
                if (outputPath == null || outputPath.trim().isEmpty()) {
                    System.err.println("Erro: parâmetro --output é obrigatório");
                    System.exit(2);
                }

                try {
                    File inputFile = new File(inputPath);
                    if (!inputFile.exists()) {
                        System.err.println("Erro: arquivo de entrada não encontrado: " + inputPath);
                        System.exit(2);
                    }
                    String content = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);
                    if (content == null || content.trim().isEmpty()) {
                        System.err.println("Erro: conteúdo a assinar não pode estar vazio");
                        System.exit(2);
                    }

                    SignRequest request = new SignRequest();
                    request.setContent(content);
                    request.setToken(token);

                    SignatureResponse response = engine.sign(request);
                    if (!response.isValid()) {
                        System.err.println("Erro ao criar assinatura: " + response.getMessage());
                        System.exit(2);
                    }

                    // Gravar assinatura no arquivo de saída
                    Files.writeString(Paths.get(outputPath), response.getSignature(), StandardCharsets.UTF_8);

                    System.out.println("Status: Sucesso");
                    System.out.println("Mensagem: " + response.getMessage());
                    System.out.println("Assinatura: " + response.getSignature());
                    System.exit(0);

                } catch (Exception e) {
                    System.err.println("Erro do sistema: " + e.getMessage());
                    System.exit(2);
                }
            } else {
                // Fallback posicional legado
                if (!paramsValidation.createSignatureParams(args)) {
                    System.exit(2);
                }
                String content = args[1];
                if (args.length > 2) {
                    token = args[2];
                }
                SignRequest request = new SignRequest();
                request.setContent(content);
                request.setToken(token);

                SignatureResponse response = engine.sign(request);
                System.out.print(formatResponse(response));
                System.exit(response.isValid() ? 0 : 2);
            }
        } else if (op == Operations.VALIDATE) {
            String inputPath = null;
            String signaturePath = null;
            String token = null;

            if (hasFlags) {
                for (int i = 1; i < args.length; i++) {
                    if ("--input".equals(args[i]) && i + 1 < args.length) {
                        inputPath = args[i + 1];
                        i++;
                    } else if ("--signature".equals(args[i]) && i + 1 < args.length) {
                        signaturePath = args[i + 1];
                        i++;
                    } else if ("--token".equals(args[i]) && i + 1 < args.length) {
                        token = args[i + 1];
                        i++;
                    }
                }

                if (inputPath == null || inputPath.trim().isEmpty()) {
                    System.err.println("Erro: parâmetro --input é obrigatório");
                    System.exit(2);
                }
                if (signaturePath == null || signaturePath.trim().isEmpty()) {
                    System.err.println("Erro: parâmetro --signature é obrigatório");
                    System.exit(2);
                }

                try {
                    File inputFile = new File(inputPath);
                    if (!inputFile.exists()) {
                        System.err.println("Erro: arquivo de entrada não encontrado: " + inputPath);
                        System.exit(2);
                    }
                    File sigFile = new File(signaturePath);
                    if (!sigFile.exists()) {
                        System.err.println("Erro: arquivo de assinatura não encontrado: " + signaturePath);
                        System.exit(2);
                    }

                    String content = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);
                    String signature = Files.readString(sigFile.toPath(), StandardCharsets.UTF_8).trim();

                    if (content == null || content.trim().isEmpty()) {
                        System.err.println("Erro: conteúdo a validar não pode estar vazio");
                        System.exit(2);
                    }
                    if (signature == null || signature.trim().isEmpty()) {
                        System.err.println("Erro: assinatura não pode estar vazia");
                        System.exit(2);
                    }

                    ValidateRequest request = new ValidateRequest();
                    request.setContent(content);
                    request.setSignature(signature);
                    request.setToken(token);

                    SignatureResponse response = engine.validate(request);

                    System.out.println("Status: " + (response.isValid() ? "Sucesso" : "Falha"));
                    System.out.println("Mensagem: " + response.getMessage());

                    System.exit(response.isValid() ? 0 : 1); // 1 = Erro do usuário (assinatura inválida), 0 = Sucesso

                } catch (Exception e) {
                    System.err.println("Erro do sistema: " + e.getMessage());
                    System.exit(2);
                }
            } else {
                // Fallback posicional legado
                if (!paramsValidation.validateSignatureParams(args)) {
                    System.exit(2);
                }
                String content = args[1];
                String signature = args[2];
                if (args.length > 3) {
                    token = args[3];
                }
                ValidateRequest request = new ValidateRequest();
                request.setContent(content);
                request.setSignature(signature);
                request.setToken(token);

                SignatureResponse response = engine.validate(request);
                System.out.print(formatResponse(response));
                System.exit(response.isValid() ? 0 : 1);
            }
        }
    }

    public static void main(String[] args) {
        boolean isServer = false;
        String port = "8080";
        String shutdownAfter = "30";

        for (int i = 0; i < args.length; i++) {
            if ("--server".equals(args[i])) {
                isServer = true;
            } else if ("--port".equals(args[i]) && i + 1 < args.length) {
                port = args[i + 1];
            } else if ("--shutdown-after".equals(args[i]) && i + 1 < args.length) {
                shutdownAfter = args[i + 1];
            }
        }

        if (isServer) {
            System.setProperty("server.port", port);
            System.setProperty("assinador.shutdown-after", shutdownAfter);
            SpringApplication.run(AssinadorApplication.class, args);
        } else {
            SpringApplication app = new SpringApplication(AssinadorApplication.class);
            app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
            app.run(args);
        }
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
