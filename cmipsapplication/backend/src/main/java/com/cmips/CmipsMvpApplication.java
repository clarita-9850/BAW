package com.cmips;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class CmipsMvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmipsMvpApplication.class, args);
    }

}

