package com.cmips;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CmipsPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmipsPocApplication.class, args);
    }

}



