package chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@SpringBootApplication
@EnableSpringConfigured
public class SpringbootChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootChatApplication.class, args);
	}

}
