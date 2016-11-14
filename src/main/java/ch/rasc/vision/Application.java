package ch.rasc.vision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;

import ch.ralscha.extdirectspring.ExtDirectSpring;
import ch.ralscha.extdirectspring.controller.ApiController;
import ch.rasc.sse.eventbus.config.EnableSseEventBus;

@Configuration
@ComponentScan(basePackageClasses = { ExtDirectSpring.class, Application.class },
		excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
				value = ApiController.class) })
@EnableAutoConfiguration(exclude = SpringDataWebAutoConfiguration.class)
@EnableAsync
@EnableSseEventBus
public class Application {

	public static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}