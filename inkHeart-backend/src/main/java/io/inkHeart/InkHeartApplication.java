package io.inkHeart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "file:jwt.properties", ignoreResourceNotFound = true)
public class InkHeartApplication {

	public static void main(String[] args) {
		SpringApplication.run(InkHeartApplication.class, args);
	}

}
