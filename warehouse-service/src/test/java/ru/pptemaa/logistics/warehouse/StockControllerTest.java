package ru.pptemaa.logistics.warehouse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class StockControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void createsAndFindsProduct() throws Exception {
        String request = """
                {"productName":"Test laptop","quantity":5,"price":899.99}
                """;
        mvc.perform(post("/api/stock").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").isNumber())
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.price").value(899.99));
        mvc.perform(get("/api/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Test laptop"));
        mvc.perform(get("/api/stock/1"))
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
}
