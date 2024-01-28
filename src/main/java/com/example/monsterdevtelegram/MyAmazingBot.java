package com.example.monsterdevtelegram;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import com.example.monsterdevtelegram.constant.Constants;
import com.example.monsterdevtelegram.helper.TwitterOauthHeaderGenerator;
import com.twitter.clientlib.model.TweetCreateResponse;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;
import io.github.redouane59.twitter.signature.TwitterCredentials;

@Component
public class MyAmazingBot extends TelegramLongPollingBot {

    @Autowired
    TwitterOauthHeaderGenerator generator;
    @Autowired TwitterClient twitterClient;


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


       /* Map<String, String> requestParams = new HashMap<>();
        String header = generator.generateHeader(HttpMethod.GET.name(), Constants.POST_TWEET, requestParams);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", header);
        Consumer<HttpHeaders> consumer = it -> it.addAll(headers);
        com.twitter.clientlib.model.Tweet tweet=new com.twitter.clientlib.model.Tweet();
        tweet.setText("hello world java");


        RestClient twitterRestClient = RestClient.builder().baseUrl("https://api.twitter.com/2").defaultHeaders(consumer).build();
        TweetCreateResponse tweetCreateResponse=twitterRestClient.post().uri("https://api.twitter.com/2/tweets").body(tweet).retrieve().body(TweetCreateResponse.class);
        System.out.println(tweetCreateResponse);
        System.out.println("testtt"+update);*/
        //https://api.twitter.com/2/tweets
        //https://developer.twitter.com/en/docs/twitter-api/tweets/manage-tweets/api-reference/post-tweets


        String tweetText=truncateString(update.getChannelPost().getText(), 120);;

        TweetParameters tweetParameters= TweetParameters.builder().text(tweetText).build();
        try{
            Tweet tweet=twitterClient.postTweet(tweetParameters);
            System.out.println("tweet posted: "+tweetText);

        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }

    }

    public static String truncateString(String input, int maxLength) {
        if (input.length() <= maxLength) {
            return input; // return the original string if it's already within the limit
        } else {
            return input.substring(0, maxLength); // return the truncated string
        }
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }
}

