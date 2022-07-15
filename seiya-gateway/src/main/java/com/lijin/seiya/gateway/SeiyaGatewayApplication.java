package com.lijin.seiya.gateway;

import com.lijin.seiya.common.swagger.annotation.EnableSeiyaSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author lj
 * @date 2022/7/15 15:20
 */
@EnableSeiyaSwagger2
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class SeiyaGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeiyaGatewayApplication.class, args);
    }
}
