package com.cmips.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic caseEventsTopic() {
        return TopicBuilder.name("cmips-case-events")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic timesheetEventsTopic() {
        return TopicBuilder.name("cmips-timesheet-events")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic providerEventsTopic() {
        return TopicBuilder.name("cmips-provider-events")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic taskEventsTopic() {
        return TopicBuilder.name("cmips-task-events")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name("cmips-notification-events")
            .partitions(3)
            .replicas(1)
            .build();
    }
}




