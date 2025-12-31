package com.example.kafkaeventdrivenapp;

import com.example.kafkaeventdrivenapp.config.BatchSchedulerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({BatchSchedulerProperties.class})
@EnableScheduling
public class TimesheetReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimesheetReportingApplication.class, args);
    }

}
