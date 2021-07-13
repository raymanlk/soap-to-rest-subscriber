package com.soap.rest;

import io.prometheus.client.CollectorRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({PrometheusScrapeEndpoint.class, CollectorRegistry.class})
public class BusinessTemplateApplication {
	public static void main(String[] args) {
		SpringApplication.run(BusinessTemplateApplication.class, args);
	}



}
