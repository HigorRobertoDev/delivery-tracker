package com.delivery.dto;

import com.delivery.model.Order;
import com.delivery.model.OrderItem;
import com.delivery.model.OrderStatus;
import java.time.Instant;
import java.util.List;

public class OrderResponse {

    private Long id;
    private String customerName;
    private String deliveryAddress;
    private OrderStatus status;
    private List<ItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.id = order.getId();
        response.customerName = order.getCustomerName();
        response.deliveryAddress = order.getDeliveryAddress();
        response.status = order.getStatus();
        response.items = order.getItems().stream().map(ItemResponse::from).toList();
        response.createdAt = order.getCreatedAt();
        response.updatedAt = order.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<ItemResponse> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class ItemResponse {
        private String name;
        private Integer quantity;

        public static ItemResponse from(OrderItem item) {
            ItemResponse response = new ItemResponse();
            response.name = item.getName();
            response.quantity = item.getQuantity();
            return response;
        }

        public String getName() {
            return name;
        }

        public Integer getQuantity() {
            return quantity;
        }
    }
}
