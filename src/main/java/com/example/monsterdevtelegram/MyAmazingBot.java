package com.example.monsterdevtelegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;

import static com.example.monsterdevtelegram.Util.truncateString;

@Component
public class MyAmazingBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String telegramBotUsername;

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Autowired TwitterClient twitterClient;


    @Override
    public String getBotUsername() {
        return telegramBotUsername;
    }

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String tweetText=truncateString(update.getChannelPost().getText(), 120);;
        TweetParameters tweetParameters= TweetParameters.builder().text(tweetText).build();
        try{
            Tweet tweet=twitterClient.postTweet(tweetParameters);
            System.out.println("tweet posted: "+tweetText);

        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }
}

