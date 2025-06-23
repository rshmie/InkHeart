package io.inkHeart.cli.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterServiceTest {
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockResponse;
    private RegisterService registerService;

    @BeforeEach
    public void setup() {
        mockHttpClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);
        registerService = new RegisterService(mockHttpClient);
    }

    @Test
    public void testHandleSignUpSuccess() throws Exception {
        String email = "test@example.com";
        String password = "securePass123";

        when(mockResponse.body()).thenReturn("Registered successfully");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        HttpResponse<String> response = registerService.handleSignUp(email, password);

        assertEquals("Registered successfully", response.body());
        var requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any());
        String body = requestCaptor.getValue().bodyPublisher().get()
                .contentLength() > 0 ? "Body exists" : "Missing";

        assertEquals("Body exists", body);
    }

}