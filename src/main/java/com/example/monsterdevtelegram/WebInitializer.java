package com.example.monsterdevtelegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import jakarta.annotation.PostConstruct;

@Component
public class WebInitializer {

    @Autowired
    TelegramBotsApi telegramBotsApi;
    @Autowired
    MyAmazingBot myAmazingBot;

    @PostConstruct
    void postconstrustmethod() {
        try {
            telegramBotsApi.registerBot(myAmazingBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
