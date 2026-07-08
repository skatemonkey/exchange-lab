package dev.exchangelab.exchange.presentation;

import dev.exchangelab.common.dto.PlaceLimitOrderRequest;
import dev.exchangelab.common.dto.PlaceLimitOrderResponse;
import dev.exchangelab.exchange.application.PlaceLimitOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PlaceLimitOrderService placeLimitOrderService;

    @PostMapping("/limit")
    public ResponseEntity<PlaceLimitOrderResponse> placeLimitOrder(
            @RequestBody PlaceLimitOrderRequest request
    ) {
        PlaceLimitOrderResponse response = placeLimitOrderService.placeLimitOrder(request);
        return ResponseEntity.accepted().body(response);
    }
}
