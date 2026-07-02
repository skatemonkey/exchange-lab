package dev.exchangelab.infrastructure.persistence.repository;

import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.infrastructure.persistence.dao.OrderDao;
import dev.exchangelab.infrastructure.persistence.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {

    private final OrderDao orderDao;

    @Override
    public List<Order> findMatchingOrdersFor(Order incomingOrder) {
        return switch (incomingOrder.getSide()) {
            case BUY -> findMatchableSellOrders(
                    incomingOrder.getSymbol(),
                    incomingOrder.getLimitPrice()
            );
            case SELL -> findMatchableBuyOrders(
                    incomingOrder.getSymbol(),
                    incomingOrder.getLimitPrice()
            );
        };
    }

    private List<Order> findMatchableBuyOrders(String symbol, BigDecimal sellLimitPrice) {
        return orderDao.findMatchableBuyOrders(symbol, sellLimitPrice)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private List<Order> findMatchableSellOrders(String symbol, BigDecimal buyLimitPrice) {
        return orderDao.findMatchableSellOrders(symbol, buyLimitPrice)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<Order> orders) {
        orderDao.saveAll(orders.stream().map(this::toEntity).toList());
    }

    private Order toDomain(OrderEntity entity) {
        return new Order(
                entity.getOrderId(),
                entity.getTraderId(),
                entity.getSymbol(),
                entity.getSide(),
                entity.getLimitPrice(),
                entity.getQuantity(),
                entity.getRemainingQuantity(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    private OrderEntity toEntity(Order order) {
        return new OrderEntity(
                order.getOrderId(),
                order.getTraderId(),
                order.getSymbol(),
                order.getSide(),
                order.getLimitPrice(),
                order.getQuantity(),
                order.getRemainingQuantity(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
