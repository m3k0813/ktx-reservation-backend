package com.ktcloudinfra.ktx_reservation.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version("v1.0")
                .title("KTX 예매 API")
                .description("KTX Project API");
        return new OpenAPI()
                .info(info);
    }
}
