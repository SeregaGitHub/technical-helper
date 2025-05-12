package ru.kraser.technical_helper.gateway.util.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    //public static final int TIMEOUT = 10000;

    @Bean
    public WebClient webClient() {
        /*final var tcpClient = TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS));
                });*/

        return WebClient.builder()
                //.baseUrl(MAIN_SERVER_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                //.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .build();
    }
}
