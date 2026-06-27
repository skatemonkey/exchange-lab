package dev.exchangelab.order.api;

import dev.exchangelab.order.api.dto.PlaceLimitOrderRequest;
import dev.exchangelab.order.api.dto.PlaceLimitOrderResponse;
import dev.exchangelab.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/limit")
    public ResponseEntity<PlaceLimitOrderResponse> placeLimitOrder(
            @RequestBody PlaceLimitOrderRequest request
    ) {
        PlaceLimitOrderResponse response = orderService.placeLimitOrder(request);
        return ResponseEntity.accepted().body(response);
    }
}
