package org.salgar.service.healthchecker;

import javax.inject.Named;

import org.salgar.healthcheck.RestHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthCheckerFactory {
	@Autowired
	@Named("proxyOrderService")
	private org.salgar.order.api.OrderService orderService;
	
	@Bean(name = "healthCheckerV2OrderService")
	public RestHealthIndicator<org.salgar.order.api.OrderService> getHealtIndicator() {
		return new RestHealthIndicator<org.salgar.order.api.OrderService>(orderService);
	}
}