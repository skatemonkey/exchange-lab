package dev.exchangelab.presentation;

import dev.exchangelab.application.PlaceLimitOrderCommand;
import dev.exchangelab.application.PlaceLimitOrderResult;
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
        PlaceLimitOrderResult result = placeLimitOrderUseCase.placeLimitOrder(toCommand(request));
        return ResponseEntity.accepted().body(PlaceLimitOrderResponse.from(result));
    }

    private PlaceLimitOrderCommand toCommand(PlaceLimitOrderRequest request) {
        return new PlaceLimitOrderCommand(
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity()
        );
    }
}
