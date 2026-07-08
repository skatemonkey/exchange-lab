package dev.exchangelab.match.application.orderbook;

import dev.exchangelab.match.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderBookInitializer implements ApplicationRunner {

    private final OrderRepository orderRepository;
    private final InMemoryOrderBookRegistry orderBookRegistry;

    @Override
    public void run(ApplicationArguments args) {
        orderBookRegistry.rebuild(orderRepository.findOpenOrders());
    }
}
