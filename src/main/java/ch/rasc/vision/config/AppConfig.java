package ch.rasc.vision.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app")
@Component
public class AppConfig {
	private String credentialsPath;

	private String xodusPath;

	public String getCredentialsPath() {
		return this.credentialsPath;
	}

	public void setCredentialsPath(String credentialsPath) {
		this.credentialsPath = credentialsPath;
	}

	public String getXodusPath() {
		return this.xodusPath;
	}

	public void setXodusPath(String xodusPath) {
		this.xodusPath = xodusPath;
	}

}
