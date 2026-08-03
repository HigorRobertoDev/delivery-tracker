package com.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.dto.CreateOrderRequest;
import com.delivery.dto.OrderResponse;
import com.delivery.dto.UpdateStatusRequest;
import com.delivery.model.Order;
import com.delivery.model.OrderItem;
import com.delivery.model.OrderStatus;
import com.delivery.security.JwtAuthFilter;
import com.delivery.security.JwtService;
import com.delivery.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /api/orders deve criar pedido")
    void create_success() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("João");
        request.setDeliveryAddress("Rua A, 10");
        CreateOrderRequest.ItemRequest item = new CreateOrderRequest.ItemRequest();
        item.setName("Pizza");
        item.setQuantity(1);
        request.setItems(List.of(item));

        when(orderService.create(any(CreateOrderRequest.class))).thenReturn(buildResponse(1L, OrderStatus.RECEBIDO));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("João"))
                .andExpect(jsonPath("$.status").value("RECEBIDO"));
    }

    @Test
    @DisplayName("GET /api/orders deve listar pedidos")
    void findAll_success() throws Exception {
        when(orderService.findAll()).thenReturn(List.of(buildResponse(1L, OrderStatus.RECEBIDO)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/orders/{id} deve retornar pedido")
    void findById_success() throws Exception {
        when(orderService.findById(1L)).thenReturn(buildResponse(1L, OrderStatus.EM_PREPARO));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} deve retornar 404 quando não encontrado")
    void findById_notFound() throws Exception {
        when(orderService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status deve atualizar status")
    void updateStatus_success() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(OrderStatus.ENTREGUE);

        when(orderService.updateStatus(eq(1L), any(UpdateStatusRequest.class)))
                .thenReturn(buildResponse(1L, OrderStatus.ENTREGUE));

        mockMvc.perform(put("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    private OrderResponse buildResponse(Long id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerName("João");
        order.setDeliveryAddress("Rua A, 10");
        order.setStatus(status);
        order.setItems(List.of(new OrderItem("Pizza", 1)));
        return OrderResponse.from(order);
    }
}
