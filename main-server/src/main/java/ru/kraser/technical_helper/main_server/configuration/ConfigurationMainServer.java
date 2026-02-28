package ru.kraser.technical_helper.main_server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class ConfigurationMainServer {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}

