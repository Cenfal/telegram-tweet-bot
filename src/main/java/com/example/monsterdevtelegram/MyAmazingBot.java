package com.example.monsterdevtelegram;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.ProfanityAction;
import com.azure.ai.translation.text.models.ProfanityMarker;
import com.azure.ai.translation.text.models.TextType;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.Translation;
import com.azure.core.credential.AzureKeyCredential;

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

    @Autowired TextTranslationClient textTranslationClient;


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
        ///////////////
        try{
            String tweetText=update.getChannelPost().getText();
            if (tweetText!=null){
                String translatedTweet=translateTweet(tweetText);
                String truncatedTranslatedTextTweet=truncateString(translatedTweet, 120);
                TweetParameters tweetParameters= TweetParameters.builder().text(truncatedTranslatedTextTweet).build();
                Tweet tweet=twitterClient.postTweet(tweetParameters);
                System.out.println("tweet posted: "+tweet.getText());
            }
        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }

    }

    private String translateTweet(String originalTweet){
        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("tr");
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem(originalTweet));

        List<TranslatedTextItem> translations = textTranslationClient.translate(targetLanguages, content, null, from, TextType.PLAIN, null, ProfanityAction.NO_ACTION, ProfanityMarker.ASTERISK, false, false, null, null, null, false);
        return translations.get(0).getTranslations().get(0).getText();
    }
    @Override
    public void clearWebhook() {

    }
}

