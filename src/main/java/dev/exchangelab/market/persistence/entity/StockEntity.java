package dev.exchangelab.market.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "stocks")
public class StockEntity {

    @Id
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    protected StockEntity() {
    }

    public StockEntity(String symbol, String name, String description) {
        this.symbol = symbol;
        this.name = name;
        this.description = description;
    }
}
