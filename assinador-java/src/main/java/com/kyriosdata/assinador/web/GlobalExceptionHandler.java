package com.kyriosdata.assinador.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OperationOutcome> handleIllegalArgumentException(IllegalArgumentException ex) {
        String fullMessage = ex.getMessage();
        String code = "value";
        String detailsText = fullMessage;

        if (fullMessage != null && fullMessage.contains(":")) {
            int colonIdx = fullMessage.indexOf(':');
            String potentialCode = fullMessage.substring(0, colonIdx).trim();
            if (potentialCode.matches("^[A-Z0-9_.-]+$")) {
                code = potentialCode;
                detailsText = fullMessage.substring(colonIdx + 1).trim();
            }
        }

        Issue issue = new Issue("error", code, detailsText);
        OperationOutcome outcome = new OperationOutcome(Collections.singletonList(issue));
        return ResponseEntity.badRequest().body(outcome);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OperationOutcome> handleGenericException(Exception ex) {
        Issue issue = new Issue("error", "exception", ex.getMessage());
        OperationOutcome outcome = new OperationOutcome(Collections.singletonList(issue));
        return ResponseEntity.internalServerError().body(outcome);
    }

    // Estruturas de DTO para corresponder ao recurso FHIR OperationOutcome
    public static class OperationOutcome {
        private final String resourceType = "OperationOutcome";
        private final List<Issue> issue;

        public OperationOutcome(List<Issue> issue) {
            this.issue = issue;
        }

        public String getResourceType() {
            return resourceType;
        }

        public List<Issue> getIssue() {
            return issue;
        }
    }

    public static class Issue {
        private final String severity;
        private final String code;
        private final Details details;

        public Issue(String severity, String code, String text) {
            this.severity = severity;
            this.code = code;
            this.details = new Details(text);
        }

        public String getSeverity() {
            return severity;
        }

        public String getCode() {
            return code;
        }

        public Details getDetails() {
            return details;
        }
    }

    public static class Details {
        private final String text;

        public Details(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
