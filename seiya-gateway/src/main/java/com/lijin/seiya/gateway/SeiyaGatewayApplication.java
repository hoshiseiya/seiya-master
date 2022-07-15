package com.lijin.seiya.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author lj
 * @date 2022/7/15 15:20
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SeiyaGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeiyaGatewayApplication.class, args);
    }
}
