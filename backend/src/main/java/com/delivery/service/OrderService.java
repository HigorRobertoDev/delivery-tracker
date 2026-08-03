package com.delivery.service;

import com.delivery.dto.CreateOrderRequest;
import com.delivery.dto.OrderResponse;
import com.delivery.dto.UpdateStatusRequest;
import com.delivery.model.Order;
import com.delivery.model.OrderItem;
import com.delivery.repository.OrderRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName().trim());
        order.setDeliveryAddress(request.getDeliveryAddress().trim());
        order.setItems(request.getItems().stream()
                .map(item -> new OrderItem(item.getName().trim(), item.getQuantity()))
                .toList());

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return OrderResponse.from(getOrder(id));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateStatusRequest request) {
        Order order = getOrder(id);
        order.setStatus(request.getStatus());
        return OrderResponse.from(orderRepository.save(order));
    }

    private Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
    }
}
