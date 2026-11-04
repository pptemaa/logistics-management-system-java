package ru.pptemaa.logistics.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;

    @Test
    void registersUserWithoutExposingPassword() throws Exception {
        String body = """
                {"firstName":"Artem","lastName":"Portnov",
                 "email":"Artem@Example.com","password":"secure-pass-123"}
                """;
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("artem@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User user = users.findByEmail("artem@example.com").orElseThrow();
        assertThat(new BCryptPasswordEncoder().matches("secure-pass-123", user.passwordHash())).isTrue();

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"A","lastName":"","email":"wrong","password":"short"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
