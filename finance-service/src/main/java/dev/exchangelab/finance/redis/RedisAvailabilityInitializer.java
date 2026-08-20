package dev.exchangelab.finance.redis;

import dev.exchangelab.finance.persistence.StockPositionRepository;
import dev.exchangelab.finance.persistence.TraderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RedisAvailabilityInitializer implements ApplicationRunner {

    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;
    private final RedisReservationService redisReservationService;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        traderAccountRepository.findAll().forEach(account ->
                redisReservationService.setAvailableCash(
                        account.getTraderId(),
                        account.toDomain().availableCash()
                ));

        stockPositionRepository.findAll().forEach(position ->
                redisReservationService.setAvailableStock(
                        position.getTraderId(),
                        position.getSymbol(),
                        position.toDomain().availableQuantity()
                ));
    }
}
