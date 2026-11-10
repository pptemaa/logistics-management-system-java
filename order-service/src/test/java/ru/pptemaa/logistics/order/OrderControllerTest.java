package ru.pptemaa.logistics.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.pptemaa.logistics.contracts.driver.DriverInfo;
import ru.pptemaa.logistics.contracts.warehouse.ProductInfo;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean WarehouseCatalogClient warehouse;
    @MockitoBean DriverDirectoryClient drivers;

    @BeforeEach
    void setUp() {
        when(warehouse.getProduct(10)).thenReturn(product(10, "Laptop", 10050, 10));
        when(warehouse.getProduct(11)).thenReturn(product(11, "Mouse", 2000, 10));
        when(warehouse.getProduct(12)).thenReturn(product(12, "Phone", 5000, 10));
        when(drivers.getDriver(42)).thenReturn(DriverInfo.newBuilder()
                .setDriverId(42).setStatus("available").build());
        when(drivers.getDriver(43)).thenReturn(DriverInfo.newBuilder()
                .setDriverId(43).setStatus("busy").build());
    }

    private ProductInfo product(long id, String name, long cents, int quantity) {
        return ProductInfo.newBuilder().setProductId(id).setProductName(name)
                .setPriceCents(cents).setAvailableQuantity(quantity).build();
    }

    @Test
    void createsOrderAndCalculatesTotal() throws Exception {
        String created = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":7,"deliveryAddress":"Moscow, Lenina 1","items":[
                                  {"productId":10,"quantity":2},
                                  {"productId":11,"quantity":3}
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
                                  {"productId":1,"quantity":0}
                                ]}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/orders/99999")).andExpect(status().isNotFound());
    }

    @Test
    void rejectsOrderWhenWarehouseHasInsufficientStock() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":7,"deliveryAddress":"Moscow, Lenina 1","items":[
                                  {"productId":10,"quantity":11}
                                ]}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void followsDeliveryLifecycleAndRejectsSkippedSteps() throws Exception {
        String created = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":8,"deliveryAddress":"Moscow, Tverskaya 1","items":[
                                  {"productId":12,"quantity":1}
                                ]}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        changeStatus(id, "{\"status\":\"delivered\"}").andExpect(status().isConflict());
        changeStatus(id, "{\"status\":\"confirmed\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("confirmed"));
        changeStatus(id, "{\"status\":\"route_ready\"}").andExpect(status().isOk());
        changeStatus(id, "{\"status\":\"assigned\"}").andExpect(status().isConflict());
        changeStatus(id, "{\"status\":\"assigned\",\"driverId\":43}")
                .andExpect(status().isConflict());
        changeStatus(id, "{\"status\":\"assigned\",\"driverId\":42}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.driverId").value(42));
        changeStatus(id, "{\"status\":\"in_progress\"}").andExpect(status().isOk());
        changeStatus(id, "{\"status\":\"delivered\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("delivered"));
        changeStatus(id, "{\"status\":\"cancelled\"}").andExpect(status().isConflict());
        changeStatus(99999, "{\"status\":\"confirmed\"}").andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet.ResultActions changeStatus(long id, String body)
            throws Exception {
        return mvc.perform(patch("/api/orders/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
