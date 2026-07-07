package dev.exchangelab.infrastructure.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisReservationService {

    private static final int STORAGE_SCALE = 8;
    private static final long RESERVE_MISSING = -1L;
    private static final long RESERVE_INSUFFICIENT = 0L;
    private static final long RESERVE_SUCCESS = 1L;
    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return -1
            end

            local remaining = redis.call('DECRBY', KEYS[1], ARGV[1])
            if remaining < 0 then
                redis.call('INCRBY', KEYS[1], ARGV[1])
                return 0
            end

            return 1
            """, Long.class);
    private static final DefaultRedisScript<Long> RESERVE_WITH_FALLBACK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                redis.call('SET', KEYS[1], ARGV[2])
            end

            local remaining = redis.call('DECRBY', KEYS[1], ARGV[1])
            if remaining < 0 then
                redis.call('INCRBY', KEYS[1], ARGV[1])
                return 0
            end

            return 1
            """, Long.class);
    private static final DefaultRedisScript<Long> INCREASE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                redis.call('SET', KEYS[1], ARGV[2])
            end

            redis.call('INCRBY', KEYS[1], ARGV[1])
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisReservationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setAvailableCash(UUID traderId, BigDecimal amount) {
        setAvailable(cashAvailableKey(traderId), amount);
    }

    public Optional<BigDecimal> findAvailableCash(UUID traderId) {
        return findAvailable(cashAvailableKey(traderId));
    }

    public void reserveCash(UUID traderId, BigDecimal amount) {
        reserve(
                cashAvailableKey(traderId),
                amount,
                "Available cash is not loaded in Redis",
                "Trader does not have enough available cash"
        );
    }

    public boolean reserveCashIfLoaded(UUID traderId, BigDecimal amount) {
        return reserveIfLoaded(
                cashAvailableKey(traderId),
                amount,
                "Trader does not have enough available cash"
        );
    }

    public void reserveCash(UUID traderId, BigDecimal amount, BigDecimal availableIfMissing) {
        reserveWithFallback(
                cashAvailableKey(traderId),
                amount,
                availableIfMissing,
                "Trader does not have enough available cash"
        );
    }

    public void increaseAvailableCash(
            UUID traderId,
            BigDecimal amount,
            BigDecimal availableIfMissing
    ) {
        increase(cashAvailableKey(traderId), amount, availableIfMissing);
    }

    public void setAvailableStock(UUID traderId, String symbol, BigDecimal amount) {
        setAvailable(stockAvailableKey(traderId, symbol), amount);
    }

    public Optional<BigDecimal> findAvailableStock(UUID traderId, String symbol) {
        return findAvailable(stockAvailableKey(traderId, symbol));
    }

    public void reserveStock(UUID traderId, String symbol, BigDecimal amount) {
        reserve(
                stockAvailableKey(traderId, symbol),
                amount,
                "Available stock is not loaded in Redis",
                "Trader does not have enough available stock"
        );
    }

    public boolean reserveStockIfLoaded(UUID traderId, String symbol, BigDecimal amount) {
        return reserveIfLoaded(
                stockAvailableKey(traderId, symbol),
                amount,
                "Trader does not have enough available stock"
        );
    }

    public void reserveStock(
            UUID traderId,
            String symbol,
            BigDecimal amount,
            BigDecimal availableIfMissing
    ) {
        reserveWithFallback(
                stockAvailableKey(traderId, symbol),
                amount,
                availableIfMissing,
                "Trader does not have enough available stock"
        );
    }

    public void increaseAvailableStock(
            UUID traderId,
            String symbol,
            BigDecimal amount,
            BigDecimal availableIfMissing
    ) {
        increase(stockAvailableKey(traderId, symbol), amount, availableIfMissing);
    }

    private void setAvailable(String key, BigDecimal amount) {
        validateNonNegative(amount);
        redisTemplate.opsForValue().set(key, toScaledAmount(amount));
    }

    private Optional<BigDecimal> findAvailable(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .map(this::fromScaledAmount);
    }

    private void reserve(
            String key,
            BigDecimal amount,
            String missingMessage,
            String insufficientMessage
    ) {
        validatePositive(amount);

        Long result = redisTemplate.execute(
                RESERVE_SCRIPT,
                List.of(key),
                toScaledAmount(amount)
        );

        if (result == RESERVE_MISSING) {
            throw new IllegalStateException(missingMessage);
        }
        if (result == RESERVE_INSUFFICIENT) {
            throw new IllegalStateException(insufficientMessage);
        }
        if (result != RESERVE_SUCCESS) {
            throw new IllegalStateException("Could not reserve available amount in Redis");
        }
    }

    private boolean reserveIfLoaded(String key, BigDecimal amount, String insufficientMessage) {
        validatePositive(amount);

        Long result = redisTemplate.execute(
                RESERVE_SCRIPT,
                List.of(key),
                toScaledAmount(amount)
        );

        if (result == RESERVE_MISSING) {
            return false;
        }
        if (result == RESERVE_INSUFFICIENT) {
            throw new IllegalStateException(insufficientMessage);
        }
        if (result != RESERVE_SUCCESS) {
            throw new IllegalStateException("Could not reserve available amount in Redis");
        }

        return true;
    }

    private void reserveWithFallback(
            String key,
            BigDecimal amount,
            BigDecimal availableIfMissing,
            String insufficientMessage
    ) {
        validatePositive(amount);
        validateNonNegative(availableIfMissing);

        Long result = redisTemplate.execute(
                RESERVE_WITH_FALLBACK_SCRIPT,
                List.of(key),
                toScaledAmount(amount),
                toScaledAmount(availableIfMissing)
        );

        if (result == RESERVE_INSUFFICIENT) {
            throw new IllegalStateException(insufficientMessage);
        }
        if (result != RESERVE_SUCCESS) {
            throw new IllegalStateException("Could not reserve available amount in Redis");
        }
    }

    private void increase(String key, BigDecimal amount, BigDecimal availableIfMissing) {
        validatePositive(amount);
        validateNonNegative(availableIfMissing);

        Long result = redisTemplate.execute(
                INCREASE_SCRIPT,
                List.of(key),
                toScaledAmount(amount),
                toScaledAmount(availableIfMissing)
        );

        if (result != RESERVE_SUCCESS) {
            throw new IllegalStateException("Could not increase available amount in Redis");
        }
    }

    private String cashAvailableKey(UUID traderId) {
        return "cash:available:%s".formatted(traderId);
    }

    private String stockAvailableKey(UUID traderId, String symbol) {
        return "stock:available:%s:%s".formatted(traderId, symbol);
    }

    private void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private void validateNonNegative(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }
    }

    private String toScaledAmount(BigDecimal amount) {
        return amount
                .movePointRight(STORAGE_SCALE)
                .setScale(0, RoundingMode.UNNECESSARY)
                .toPlainString();
    }

    private BigDecimal fromScaledAmount(String scaledAmount) {
        return new BigDecimal(scaledAmount).movePointLeft(STORAGE_SCALE);
    }
}
