package com.example.monsterdevtelegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Component
public class MyAmazingBot extends TelegramLongPollingBot {



    @Override
    public String getBotUsername() {
        return "teleresistance_bot";
    }

    @Override
    public String getBotToken() {
        return "6070612880:AAGZMXNMlPlQm8nYVBRQbmYQvHOWDSxYtvU";
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("testtt"+update);
    }



    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }
}

