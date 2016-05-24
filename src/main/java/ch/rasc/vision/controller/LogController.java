package ch.rasc.vision.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.rasc.vision.Application;

@RestController
public class LogController {

	private final static String lineSeparator = System.getProperty("line.separator");

	@Async
	@PostMapping("/crashlog")
	public void logClientCrash(
			@RequestHeader(value = HttpHeaders.USER_AGENT,
					required = false) String userAgent,
			@RequestParam Map<String, String> crashData) {

		StringBuilder sb = new StringBuilder();
		sb.append("JavaScript Error");
		sb.append(lineSeparator);
		sb.append("User-Agent: " + userAgent);
		crashData.forEach((k, v) -> {
			sb.append(lineSeparator);
			sb.append(k);
			sb.append(": ");
			sb.append(v);
		});

		Application.logger.error(sb.toString());
	}

}
