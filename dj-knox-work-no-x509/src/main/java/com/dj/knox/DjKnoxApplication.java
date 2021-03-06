package com.dj.knox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = {FreeMarkerAutoConfiguration.class})
public class DjKnoxApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DjKnoxApplication.class, args);
    }
}
