package com.example.monsterdevtelegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Component
public class MyAmazingBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("updatte "+update);
    }



    @Override
    public String getBotUsername() {
        return "teleresistance_bot";
    }

    @Override
    public String getBotToken() {
        return "6070612880:AAGZMXNMlPlQm8nYVBRQbmYQvHOWDSxYtvU";
    }
}

