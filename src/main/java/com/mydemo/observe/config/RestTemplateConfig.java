package com.mydemo.observe.config;

import com.mydemo.observe.telemetry.OpenTelemetryRestTemplateInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(OpenTelemetry openTelemetry) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return new RestTemplateBuilder()
                .requestFactory(() -> requestFactory)
                .additionalInterceptors(new OpenTelemetryRestTemplateInterceptor(openTelemetry))
                .build();
    }
}
