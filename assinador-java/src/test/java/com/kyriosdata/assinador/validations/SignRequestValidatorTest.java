package com.kyriosdata.assinador.validations;

import com.kyriosdata.assinador.domain.SignRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SignRequestValidatorTest {

    private final SignRequestValidator validator = new SignRequestValidator();

    private SignRequest createValidRequest() {
        SignRequest request = new SignRequest();
        request.setBundleJson("{\"resourceType\":\"Bundle\"}");
        request.setProvenanceJson("{\"resourceType\":\"Provenance\"}");
        request.setMaterial("{\"type\":\"PEM\",\"key\":\"-----BEGIN PRIVATE KEY-----\\nMOCK\\n-----END PRIVATE KEY-----\"}");
        request.setCertChain(Arrays.asList("dGVzdA=="));
        request.setReferenceTimestamp(1751328000L); // 1º julho 2025 (mínimo)
        request.setStrategy("iat");
        request.setSignaturePolicy("https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|0.0.1");
        request.setOperationalConfig("{\"verification\":{}}");
        return request;
    }

    @Test
    void shouldPassForValidRequest() {
        SignRequest request = createValidRequest();
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldFailForNullRequest() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
        assertTrue(ex.getMessage().contains("CONFIG.INVALID-PARAMETER"));
    }

    @Test
    void shouldFailForEmptyBundle() {
        SignRequest request = createValidRequest();
        request.setBundleJson(null);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("FORMAT.BUNDLE-EMPTY"));

        request.setBundleJson("   ");
        ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("FORMAT.BUNDLE-EMPTY"));
    }

    @Test
    void shouldFailForInvalidBundleJson() {
        SignRequest request = createValidRequest();
        request.setBundleJson("{invalid json");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("FORMAT.BUNDLE-INVALID"));
    }

    @Test
    void shouldFailForEmptyMaterial() {
        SignRequest request = createValidRequest();
        request.setMaterial(null);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CONFIG.MATERIAL-EMPTY"));
    }

    @Test
    void shouldFailForInvalidMaterialType() {
        SignRequest request = createValidRequest();
        request.setMaterial("{\"type\":\"INVALID\"}");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CONFIG.MATERIAL-TYPE-INVALID"));
    }

    @Test
    void shouldFailForEmptyCertChain() {
        SignRequest request = createValidRequest();
        request.setCertChain(null);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CERT.CHAIN-EMPTY"));

        request.setCertChain(new ArrayList<>());
        ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CERT.CHAIN-EMPTY"));
    }

    @Test
    void shouldFailForTimestampOutOfRange() {
        SignRequest request = createValidRequest();
        request.setReferenceTimestamp(1700000000L); // muito antigo
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CONFIG.TIMESTAMP-OUT-OF-RANGE"));

        request.setReferenceTimestamp(4200000000L); // muito no futuro
        ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CONFIG.TIMESTAMP-OUT-OF-RANGE"));
    }

    @Test
    void shouldFailForInvalidPolicyUri() {
        SignRequest request = createValidRequest();
        request.setSignaturePolicy("https://wrong.uri/ImplementationGuide/br.go.ses.seguranca|0.0.1");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CONFIG.POLICY-URI-INVALID"));
    }

    @Test
    void shouldFailForInvalidPolicyVersion() {
        SignRequest request = createValidRequest();
        request.setSignaturePolicy("https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|alpha1");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("CONFIG.POLICY-VERSION-INVALID"));
    }
}
