package ru.pptemaa.logistics.gateway;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GatewayControllerTest {
    @Test
    @SuppressWarnings("unchecked")
    void forwardsRequestAndBackendResponse() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<byte[]> backend = mock(HttpResponse.class);
        when(backend.statusCode()).thenReturn(409);
        when(backend.body()).thenReturn("{\"error\":\"unavailable\"}".getBytes());
        when(backend.headers()).thenReturn(java.net.http.HttpHeaders.of(
                Map.of("Content-Type", List.of("application/json")),
                (name, value) -> true));
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(backend);

        var mvc = MockMvcBuilders.standaloneSetup(new GatewayController(client,
                "http://auth:8081", "http://orders:8082",
                "http://drivers:8083", "http://warehouse:8084")).build();
        mvc.perform(post("/api/orders?source=test")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":7}"))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"error\":\"unavailable\"}"));

        ArgumentCaptor<HttpRequest> request = ArgumentCaptor.forClass(HttpRequest.class);
        verify(client).send(request.capture(), any(HttpResponse.BodyHandler.class));
        org.junit.jupiter.api.Assertions.assertEquals("POST", request.getValue().method());
        org.junit.jupiter.api.Assertions.assertEquals(
                "http://orders:8082/api/orders?source=test",
                request.getValue().uri().toString());
        org.junit.jupiter.api.Assertions.assertEquals("Bearer token",
                request.getValue().headers().firstValue("Authorization").orElseThrow());
    }
}
