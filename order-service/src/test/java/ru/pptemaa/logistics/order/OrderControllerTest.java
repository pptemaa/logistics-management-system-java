package ru.pptemaa.logistics.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class OrderControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void createsOrderAndCalculatesTotal() throws Exception {
        String created = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":7,"deliveryAddress":"Moscow, Lenina 1","items":[
                                  {"productId":10,"productName":"Laptop","unitPrice":100.50,"quantity":2},
                                  {"productId":11,"productName":"Mouse","unitPrice":20.00,"quantity":3}
                                ]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.totalAmount").value(261.00))
                .andExpect(jsonPath("$.items[0].lineTotal").value(201.00))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();
        mvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalAmount").value(261.00));
    }

    @Test
    void rejectsInvalidOrderAndMissingId() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":0,"deliveryAddress":"","items":[
                                  {"productId":1,"productName":"Item","unitPrice":-2,"quantity":0}
                                ]}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/orders/99999")).andExpect(status().isNotFound());
    }
}
