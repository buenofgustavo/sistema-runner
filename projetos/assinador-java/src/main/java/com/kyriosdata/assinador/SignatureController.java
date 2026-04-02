package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignatureRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SignatureController {

    private final SignatureService signatureService;

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/sign")
    public SignatureResponse sign(@RequestBody SignatureRequest request) {
        return signatureService.sign(request);
    }

    @PostMapping("/validate")
    public SignatureResponse validate(@RequestBody SignatureRequest request) {
        return signatureService.validate(request);
    }
}