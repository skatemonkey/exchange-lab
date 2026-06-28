package dev.exchangelab.presentation;

import dev.exchangelab.application.PlaceLimitOrderUseCase;
import dev.exchangelab.presentation.dto.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.dto.PlaceLimitOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PlaceLimitOrderUseCase placeLimitOrderUseCase;

    public OrderController(PlaceLimitOrderUseCase placeLimitOrderUseCase) {
        this.placeLimitOrderUseCase = placeLimitOrderUseCase;
    }

    @PostMapping("/limit")
    public ResponseEntity<PlaceLimitOrderResponse> placeLimitOrder(
            @RequestBody PlaceLimitOrderRequest request
    ) {
        PlaceLimitOrderResponse response = placeLimitOrderUseCase.placeLimitOrder(request);
        return ResponseEntity.accepted().body(response);
    }
}
