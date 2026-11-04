package ru.pptemaa.logistics.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.JwtException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginControllerTest {
    @Autowired MockMvc mvc;
    @Autowired JwtTokenService tokens;
    @Autowired ObjectMapper json;

    @Test
    void logsInWithVerifiedTokenAndRejectsWrongPassword() throws Exception {
        String email = "login-test@example.com";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Test","lastName":"User",
                                 "email":"login-test@example.com","password":"correct-pass-123"}
                                """))
                .andExpect(status().isCreated());

        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"LOGIN-TEST@example.com","password":"correct-pass-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        JsonNode body = json.readTree(response);
        assertThat(tokens.verifiedSubject(body.get("accessToken").asText())).isNotBlank();
        assertThat(body.get("expiresAt").asText()).isNotBlank();
        String token = body.get("accessToken").asText();
        assertThatThrownBy(() -> tokens.verifiedSubject(token + "invalid"))
                .isInstanceOf(JwtException.class);

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }
}
