package com.kyriosdata.assinador.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyriosdata.assinador.SignatureService;
import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import com.kyriosdata.assinador.domain.ValidateRequest;
import com.kyriosdata.assinador.validations.SignatureParamsValidation;
import com.kyriosdata.assinador.validations.SignRequestValidator;
import com.kyriosdata.assinador.validations.ValidateRequestValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssinadorController.class)
class AssinadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SignatureService signatureService;

    @MockitoBean
    private SignatureParamsValidation signatureParamsValidation;

    @MockitoBean
    private SignRequestValidator signRequestValidator;

    @MockitoBean
    private ValidateRequestValidator validateRequestValidator;

    @MockitoBean
    private InactivityShutdownManager shutdownManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldSignSuccessfully() throws Exception {
        SignRequest request = new SignRequest();
        request.setBundleJson("{}");
        
        SignatureResponse expectedResponse = new SignatureResponse("MOCK_SIGNATURE", true, "Success");
        Mockito.when(signatureService.sign(any(SignRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signature").value("MOCK_SIGNATURE"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void shouldValidateSuccessfully() throws Exception {
        ValidateRequest request = new ValidateRequest();
        request.setJwsBase64("MOCK_JWS");

        SignatureResponse expectedResponse = new SignatureResponse("MOCK_JWS", true, "Valid");
        Mockito.when(signatureService.validate(any(ValidateRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }
}
