package com.example.monsterdevtelegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;

@Configuration
public class BeanConfig {
    @Value("${twitter.oauth.consumerKey}")
    private String consumerKey;

    @Value("${twitter.oauth.consumerSecret}")
    private String consumerSecret;

    @Value("${twitter.oauth.accessToken}")
    private String token;

    @Value("${twitter.oauth.accessTokenSecret}")
    private String tokenSecret;
    @Bean
    MyAmazingBot myAmazingBot(){
        return new MyAmazingBot();
    }
    @Bean
    TelegramBotsApi telegramBotsApi(){
        try {
            return new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    @Bean
    TwitterCredentials twitterCredentials(){
        return TwitterCredentials.builder().accessToken(token).accessTokenSecret(tokenSecret).apiKey(consumerKey).apiSecretKey(consumerSecret).build();
    }
    @Bean
    TwitterClient twitterClient(){
        return new TwitterClient(twitterCredentials());
    }





}
