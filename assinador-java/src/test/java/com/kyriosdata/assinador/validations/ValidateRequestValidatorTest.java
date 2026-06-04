package com.kyriosdata.assinador.validations;

import com.kyriosdata.assinador.domain.ValidateRequest;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ValidateRequestValidatorTest {

    private final ValidateRequestValidator validator = new ValidateRequestValidator();

    private ValidateRequest createValidRequest() {
        ValidateRequest request = new ValidateRequest();
        
        // JWS estruturalmente correto
        String jwsJson = "{"
            + "\"payload\":\"ZXk...\", "
            + "\"signatures\":[{"
            + "\"protected\":\"ZXk...\", "
            + "\"signature\":\"ZXk...\""
            + "}]"
            + "}";
        String jwsBase64 = Base64.getEncoder().encodeToString(jwsJson.getBytes());

        request.setJwsBase64(jwsBase64);
        request.setReferenceTimestamp(1751328000L); // 1º julho 2025 (mínimo)
        request.setSignaturePolicy("https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|0.0.1");
        request.setOperationalConfig("{\"verification\":{}}");
        return request;
    }

    @Test
    void shouldPassForValidRequest() {
        ValidateRequest request = createValidRequest();
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldFailForNullRequest() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
        assertTrue(ex.getMessage().contains("CONFIG.INVALID-PARAMETER"));
    }

    @Test
    void shouldFailForEmptyJws() {
        ValidateRequest request = createValidRequest();
        request.setJwsBase64(null);
        request.setSignature(null);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("FORMAT.JWS-EMPTY"));
    }

    @Test
    void shouldFailForInvalidBase64() {
        ValidateRequest request = createValidRequest();
        request.setJwsBase64("NotBase64!!!");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("FORMAT.BASE64-INVALID"));
    }

    @Test
    void shouldFailForJwsMissingPayload() {
        ValidateRequest request = createValidRequest();
        String badJwsJson = "{\"signatures\":[]}";
        String badJwsBase64 = Base64.getEncoder().encodeToString(badJwsJson.getBytes());
        request.setJwsBase64(badJwsBase64);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("FORMAT.JWS-PAYLOAD-MISSING"));
    }

    @Test
    void shouldFailForJwsMissingSignatures() {
        ValidateRequest request = createValidRequest();
        String badJwsJson = "{\"payload\":\"ZXk...\"}";
        String badJwsBase64 = Base64.getEncoder().encodeToString(badJwsJson.getBytes());
        request.setJwsBase64(badJwsBase64);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("FORMAT.JWS-SIGNATURES-MISSING"));
    }

    @Test
    void shouldFailForTimestampOutOfRange() {
        ValidateRequest request = createValidRequest();
        request.setReferenceTimestamp(1600000000L); // muito antigo
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CONFIG.TIMESTAMP-OUT-OF-RANGE"));
    }

    @Test
    void shouldFailForInvalidPolicy() {
        ValidateRequest request = createValidRequest();
        request.setSignaturePolicy("https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|v1");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CONFIG.POLICY-VERSION-INVALID"));
    }
}
