package com.example.undertowmetrics.config;

import com.example.undertowmetrics.metrics.UndertowMetricsHandlerWrapper;
import org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    UndertowDeploymentInfoCustomizer undertowDeploymentInfoCustomizer(
            UndertowMetricsHandlerWrapper undertowMetricsHandlerWrapper) {
        return deploymentInfo ->
                deploymentInfo.addOuterHandlerChainWrapper(undertowMetricsHandlerWrapper);
    }
}
