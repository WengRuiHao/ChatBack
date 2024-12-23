package chat.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	/**
	 * ------------------------------------direct模式-----------------------------------------------------------------------------
	 **/

	@Bean
	public DirectExchange TypeExchange() {
		return new DirectExchange("TypeExchange");
	}
	
	@Bean
	public Queue SendMessageToOnlineUsersQueue() {
		return new Queue("SendMessageToOnlineUsersQueue", true); // 在房間會員通道
	}

	@Bean
	public Queue SendMessageToOfflineUsersQueue() {
		return new Queue("SendMessageToOfflineUsersQueue", true); // 不在房間會員通道
	}

	@Bean
	public Binding SendMessageToOnlineUsersBinding() {
		return BindingBuilder.bind(SendMessageToOnlineUsersQueue()).to(TypeExchange()).with("SendMessageToOnlineUsers");
	}

	@Bean
	public Binding SendMessageToOfflineUsersBinding() {
		return BindingBuilder.bind(SendMessageToOfflineUsersQueue()).to(TypeExchange()).with("SendMessageToOfflineUsers");
	}

}