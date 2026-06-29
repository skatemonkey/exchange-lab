package dev.exchangelab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ExchangeLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeLabApplication.class, args);
    }

}
