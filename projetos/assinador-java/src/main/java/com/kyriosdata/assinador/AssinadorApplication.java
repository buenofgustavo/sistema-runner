package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignatureRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public final class AssinadorApplication {

    private AssinadorApplication() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0].toLowerCase(Locale.ROOT);
        switch (command) {
            case "sign" -> sign(new FakeSignatureService(), args);
            case "validate" -> validate(new FakeSignatureService(), args);
            case "api" -> SpringApplication.run(AssinadorApplication.class, args);
            default -> printUsage();
        }
    }

    private static void sign(SignatureService signatureService, String[] args) {
        if (args.length < 2) {
            printUsage();
            return;
        }

        SignatureRequest request = new SignatureRequest();
        request.setContent(args[1]);

        printResponse(signatureService.sign(request));
    }

    private static void validate(SignatureService signatureService, String[] args) {
        if (args.length < 3) {
            printUsage();
            return;
        }

        SignatureRequest request = new SignatureRequest();
        request.setContent(args[1]);
        request.setSignature(args[2]);

        printResponse(signatureService.validate(request));
    }

    private static void printResponse(SignatureResponse response) {
        System.out.println("valid=" + response.isValid());
        System.out.println("signature=" + response.getSignature());
        System.out.println("message=" + response.getMessage());
    }

    private static void printUsage() {
        System.out.println("Uso:");
        System.out.println("  sign <content>");
        System.out.println("  validate <content> <signature>");
        System.out.println("  api");
    }
}
