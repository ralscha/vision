package ch.rasc.vision.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app")
@Component
public class AppConfig {
	private String visionKey;

	public String getVisionKey() {
		return this.visionKey;
	}

	public void setVisionKey(String visionKey) {
		this.visionKey = visionKey;
	}

}
