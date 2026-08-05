package ru.kraser.technical_helper.breakage_server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class ConfigurationBreakageServer {

    @Bean(name = "breakageClock")
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
