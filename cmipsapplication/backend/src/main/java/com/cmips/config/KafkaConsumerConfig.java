package com.cmips.config;

import com.cmips.event.BaseEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, BaseEvent> baseEventConsumerFactory() {
        JsonDeserializer<BaseEvent> valueDeserializer = new JsonDeserializer<>(BaseEvent.class);
        valueDeserializer.addTrustedPackages("com.cmips.event");
        valueDeserializer.ignoreTypeHeaders();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Do not configure VALUE_DESERIALIZER_CLASS_CONFIG when passing deserializer instance
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BaseEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BaseEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(baseEventConsumerFactory());
        return factory;
    }
}


