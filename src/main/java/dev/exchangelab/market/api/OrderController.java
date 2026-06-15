package dev.exchangelab.market.api;

import dev.exchangelab.market.api.dto.PlaceLimitOrderRequest;
import dev.exchangelab.market.api.dto.PlaceLimitOrderResponse;
import dev.exchangelab.market.application.dto.PlaceLimitOrderResult;
import dev.exchangelab.market.application.PlaceLimitOrderUseCase;
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
    public ResponseEntity<PlaceLimitOrderResponse> placeLimitOrder(@RequestBody PlaceLimitOrderRequest request) {
        PlaceLimitOrderResult result = placeLimitOrderUseCase.place(request.toCommand());
        return ResponseEntity.accepted().body(PlaceLimitOrderResponse.from(result));
    }
}
