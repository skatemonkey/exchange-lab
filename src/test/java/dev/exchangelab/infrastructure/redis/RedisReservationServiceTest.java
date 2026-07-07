package dev.exchangelab.infrastructure.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class RedisReservationServiceTest {

    private static final String SYMBOL = "ACME";

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;
    private RedisReservationService reservationService;

    @BeforeEach
    void setUp() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
                redis.getHost(),
                redis.getMappedPort(6379)
        );
        connectionFactory = new LettuceConnectionFactory(configuration);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();

        reservationService = new RedisReservationService(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        connectionFactory.destroy();
    }

    @Test
    void reservesCashFromAvailableCash() {
        UUID traderId = UUID.randomUUID();
        reservationService.setAvailableCash(traderId, money("1000.25"));

        reservationService.reserveCash(traderId, money("300.25"));

        assertThat(reservationService.findAvailableCash(traderId))
                .hasValueSatisfying(amount -> assertThat(amount).isEqualByComparingTo("700"));
    }

    @Test
    void rejectsCashReservationWhenAvailableCashIsNotEnough() {
        UUID traderId = UUID.randomUUID();
        reservationService.setAvailableCash(traderId, money("100"));

        assertThatThrownBy(() -> reservationService.reserveCash(traderId, money("101")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Trader does not have enough available cash");

        assertThat(reservationService.findAvailableCash(traderId))
                .hasValueSatisfying(amount -> assertThat(amount).isEqualByComparingTo("100"));
    }

    @Test
    void reservesStockFromAvailableStock() {
        UUID traderId = UUID.randomUUID();
        reservationService.setAvailableStock(traderId, SYMBOL, quantity("10"));

        reservationService.reserveStock(traderId, SYMBOL, quantity("4"));

        assertThat(reservationService.findAvailableStock(traderId, SYMBOL))
                .hasValueSatisfying(amount -> assertThat(amount).isEqualByComparingTo("6"));
    }

    @Test
    void increasesCashFromAvailableCash() {
        UUID traderId = UUID.randomUUID();
        reservationService.setAvailableCash(traderId, money("100"));

        reservationService.increaseAvailableCash(traderId, money("50"), money("0"));

        assertThat(reservationService.findAvailableCash(traderId))
                .hasValueSatisfying(amount -> assertThat(amount).isEqualByComparingTo("150"));
    }

    @Test
    void increasesStockFromFallbackWhenAvailableStockIsMissing() {
        UUID traderId = UUID.randomUUID();

        reservationService.increaseAvailableStock(traderId, SYMBOL, quantity("3"), quantity("7"));

        assertThat(reservationService.findAvailableStock(traderId, SYMBOL))
                .hasValueSatisfying(amount -> assertThat(amount).isEqualByComparingTo("10"));
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal quantity(String value) {
        return new BigDecimal(value);
    }
}
