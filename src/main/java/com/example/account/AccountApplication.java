package com.example.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.beans.BeanProperty;

@SpringBootApplication
public class AccountApplication {


    @Bean
    public WebClient.Builder getWebClientBuilder()
    {
        return  WebClient.builder();
    }


    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }

}
