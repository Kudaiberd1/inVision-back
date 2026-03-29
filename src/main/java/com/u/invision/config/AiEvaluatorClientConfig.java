package com.u.invision.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiEvaluatorClientConfig {

	@Bean(name = "aiEvaluatorRestTemplate")
	public RestTemplate aiEvaluatorRestTemplate() {
		return new RestTemplate();
	}
}
