package com.rh360.rh360.config;

import com.rh360.rh360.realtime.RedisRealTimePublisher;
import com.rh360.rh360.realtime.RedisRealTimeSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@ConditionalOnProperty(name = "realtime.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisRealTimeConfig {

    @Bean
    public ChannelTopic realTimeChannelTopic() {
        return new ChannelTopic(RedisRealTimePublisher.CHANNEL);
    }

    @Bean
    public MessageListenerAdapter realTimeMessageListener(RedisRealTimeSubscriber subscriber) {
        // chama subscriber.onMessage(String)
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter realTimeMessageListener,
            ChannelTopic realTimeChannelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(realTimeMessageListener, realTimeChannelTopic);
        return container;
    }
}

