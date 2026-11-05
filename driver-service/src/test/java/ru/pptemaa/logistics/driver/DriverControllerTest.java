package ru.pptemaa.logistics.driver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DriverControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void createsAndFindsDriver() throws Exception {
        String request = """
                {"name":"Ivan Petrov","phone":"+79990000000",
                 "licenseNumber":"TEST-123","car":"Lada Vesta"}
                """;
        mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("offline"));
        mvc.perform(get("/api/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].licenseNumber").value("TEST-123"));
        mvc.perform(get("/api/drivers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan Petrov"));
        mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidDriverAndMissingId() throws Exception {
        mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","phone":"","licenseNumber":"","car":""}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/drivers/99999")).andExpect(status().isNotFound());
    }

    @Test
    void changesAvailabilityAndFiltersDrivers() throws Exception {
        String created = mvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Anna Sidorova","phone":"+79991112233",
                                 "licenseNumber":"STATUS-456","car":"Kia Rio"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(created).get("id").asLong();
        mvc.perform(patch("/api/drivers/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"available\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("available"));
        mvc.perform(get("/api/drivers").param("status", "available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].licenseNumber").value("STATUS-456"));
        mvc.perform(patch("/api/drivers/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"unknown\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/drivers").param("status", "unknown"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/drivers/99999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"available\"}"))
                .andExpect(status().isNotFound());
    }
}
