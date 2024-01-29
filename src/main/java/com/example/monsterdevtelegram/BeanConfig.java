package com.example.monsterdevtelegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.core.credential.AzureKeyCredential;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;

@Configuration
@Import({ TwitterAuthConfig.class, MSTranslatorConfig.class })
public class BeanConfig {

    @Autowired
    TwitterAuthConfig twitterAuthConfig;

    @Autowired
    MSTranslatorConfig msTranslatorConfig;

    @Bean
    MyAmazingBot myAmazingBot() {
        return new MyAmazingBot();
    }

    @Bean
    TelegramBotsApi telegramBotsApi() {
        try {
            return new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    TwitterCredentials twitterCredentials() {
        return TwitterCredentials.builder().accessToken(twitterAuthConfig.getToken()).accessTokenSecret(twitterAuthConfig.getTokenSecret()).apiKey(twitterAuthConfig.getConsumerKey()).apiSecretKey(twitterAuthConfig.getConsumerSecret()).build();
    }

    @Bean
    TwitterClient twitterClient() {
        return new TwitterClient(twitterCredentials());
    }

    @Bean
    TextTranslationClient msTextTranslationClient() {
        AzureKeyCredential credential = new AzureKeyCredential(msTranslatorConfig.getMicrosoftTranslatorKey());

        return new TextTranslationClientBuilder().credential(credential).region("germanywestcentral").buildClient();
    }

}
