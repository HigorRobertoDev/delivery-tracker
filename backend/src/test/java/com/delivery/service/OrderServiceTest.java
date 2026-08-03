package com.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.delivery.dto.CreateOrderRequest;
import com.delivery.dto.OrderResponse;
import com.delivery.dto.UpdateStatusRequest;
import com.delivery.model.Order;
import com.delivery.model.OrderItem;
import com.delivery.model.OrderStatus;
import com.delivery.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("deve criar pedido com status RECEBIDO e persistir itens")
    void create_success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName(" João ");
        request.setDeliveryAddress(" Rua A, 10 ");

        CreateOrderRequest.ItemRequest item = new CreateOrderRequest.ItemRequest();
        item.setName(" Pizza ");
        item.setQuantity(2);
        request.setItems(List.of(item));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            return order;
        });

        OrderResponse response = orderService.create(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCustomerName()).isEqualTo("João");
        assertThat(response.getDeliveryAddress()).isEqualTo("Rua A, 10");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.RECEBIDO);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getName()).isEqualTo("Pizza");
        assertThat(response.getItems().getFirst().getQuantity()).isEqualTo(2);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getName()).isEqualTo("Pizza");
        assertThat(saved.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("deve listar todos os pedidos")
    void findAll_success() {
        Order order = buildOrder(1L, OrderStatus.RECEBIDO);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getId()).isEqualTo(1L);
        assertThat(responses.getFirst().getCustomerName()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("deve buscar pedido por ID")
    void findById_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(buildOrder(1L, OrderStatus.EM_PREPARO)));

        OrderResponse response = orderService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.EM_PREPARO);
    }

    @Test
    @DisplayName("deve lançar 404 quando pedido não existe")
    void findById_notFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusException = (ResponseStatusException) ex;
                    assertThat(statusException.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(statusException.getReason()).isEqualTo("Pedido não encontrado");
                });
    }

    @Test
    @DisplayName("deve atualizar status do pedido")
    void updateStatus_success() {
        Order order = buildOrder(1L, OrderStatus.RECEBIDO);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(OrderStatus.SAIU_PARA_ENTREGA);

        OrderResponse response = orderService.updateStatus(1L, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.SAIU_PARA_ENTREGA);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("deve lançar 404 ao atualizar status de pedido inexistente")
    void updateStatus_notFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(OrderStatus.CANCELADO);

        assertThatThrownBy(() -> orderService.updateStatus(99L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusException = (ResponseStatusException) ex;
                    assertThat(statusException.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });

        verify(orderRepository, never()).save(any());
    }

    private Order buildOrder(Long id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerName("Maria");
        order.setDeliveryAddress("Rua B, 20");
        order.setStatus(status);
        order.setItems(List.of(new OrderItem("Hambúrguer", 1)));
        return order;
    }
}
