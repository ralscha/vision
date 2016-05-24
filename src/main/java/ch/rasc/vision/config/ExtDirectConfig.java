package ch.rasc.vision.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ralscha.extdirectspring.util.JsonHandler;

@Configuration
public class ExtDirectConfig {

	@Bean
	public ch.ralscha.extdirectspring.controller.Configuration configuration() {
		ch.ralscha.extdirectspring.controller.Configuration config = new ch.ralscha.extdirectspring.controller.Configuration();
		config.setMaxRetries(0);
		return config;
	}

	@Bean
	public JsonHandler jsonHandler(ObjectMapper objectMapper) {
		JsonHandler jh = new JsonHandler();
		jh.setMapper(objectMapper);
		return jh;
	}

}
