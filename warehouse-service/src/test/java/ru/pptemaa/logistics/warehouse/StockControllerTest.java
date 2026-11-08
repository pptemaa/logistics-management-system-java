package ru.pptemaa.logistics.warehouse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;

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
class StockControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void createsAndFindsProduct() throws Exception {
        String request = """
                {"productName":"Test laptop","quantity":5,"price":899.99}
                """;
        String created = mvc.perform(post("/api/stock").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").isNumber())
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.price").value(899.99))
                .andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(created).get("productId").asLong();
        mvc.perform(get("/api/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].productName", hasItem("Test laptop")));
        mvc.perform(get("/api/stock/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Test laptop"));
        mvc.perform(post("/api/stock").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidProduct() throws Exception {
        mvc.perform(post("/api/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productName":"","quantity":-1,"price":-2}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/stock/99999")).andExpect(status().isNotFound());
    }

    @Test
    void reservesOnlyAvailableQuantityAndReplenishes() throws Exception {
        String created = mvc.perform(post("/api/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productName":"Test monitor","quantity":3,"price":199.50}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(created).get("productId").asLong();

        mvc.perform(post("/api/stock/{id}/reserve", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(1));
        mvc.perform(post("/api/stock/{id}/reserve", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":2}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/stock/{id}", id))
                .andExpect(jsonPath("$.quantity").value(1));
        mvc.perform(post("/api/stock/{id}/replenish", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(6));
        mvc.perform(post("/api/stock/{id}/reserve", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/stock/99999/replenish")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }
}
