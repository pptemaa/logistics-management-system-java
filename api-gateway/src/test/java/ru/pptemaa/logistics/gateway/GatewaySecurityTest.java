package ru.pptemaa.logistics.gateway;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "JWT_SECRET=local-test-secret-with-at-least-32-bytes")
class GatewaySecurityTest {
    @Autowired MockMvc mvc;

    @Test
    void requiresTokenForBusinessRoutes() throws Exception {
        mvc.perform(get("/api/orders/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/drivers")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/stock")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/orders/1").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointRemainsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
