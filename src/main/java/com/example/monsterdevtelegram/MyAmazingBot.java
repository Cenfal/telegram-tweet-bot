package com.example.monsterdevtelegram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.ProfanityAction;
import com.azure.ai.translation.text.models.ProfanityMarker;
import com.azure.ai.translation.text.models.TextType;
import com.azure.ai.translation.text.models.TranslatedTextItem;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.MediaCategory;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;
import io.github.redouane59.twitter.dto.tweet.UploadMediaResponse;

import static com.example.monsterdevtelegram.Util.truncateString;

@Component
public class MyAmazingBot extends TelegramLongPollingBot {
    Logger logger = Logger.getLogger(MyAmazingBot.class.getName());

    @Value("${telegram.bot.username}")
    private String telegramBotUsername;

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Autowired
    TwitterClient twitterClient;

    @Autowired
    TextTranslationClient textTranslationClient;

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
        logger.log(Level.INFO, "Update received with update id: "+update.getUpdateId());
        Message message = update.getChannelPost();
        List<PhotoSize> photos = message.getPhoto();
        String getID = null;
        if (photos.size()>0){
            getID=savePhotoToResources(photos);
        }

        try {
            String tweetText = update.getChannelPost().getText();
            if (tweetText != null) {
                postTextTweet(tweetText);
            }
            String caption = update.getChannelPost().getCaption();
            if (caption != null) {
                String translatedTweet = translateTweet(caption);
                Optional<UploadMediaResponse> uploadMediaResponse = twitterClient.uploadChunkedMedia(new java.io.File("./resources/telgramphoto/" + getID + ".jpg"), MediaCategory.TWEET_IMAGE);
                if (uploadMediaResponse.isPresent()) {
                    String truncatedTranslatedTextTweet = truncateString(translatedTweet, 120);
                    //post tweet with media and text
                    postTweet(TweetParameters.builder().text(truncatedTranslatedTextTweet), uploadMediaResponse);

                }
            } else {
                Optional<UploadMediaResponse> uploadMediaResponse = twitterClient.uploadChunkedMedia(new java.io.File("./resources/telgramphoto/" + getID + ".jpg"), MediaCategory.TWEET_IMAGE);
                if (uploadMediaResponse.isPresent()) {
                    //post tweet with media-only
                    postTweet(TweetParameters.builder(), uploadMediaResponse);
                }
        }
        }catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    private void postTweet(TweetParameters.TweetParametersBuilder builder, Optional<UploadMediaResponse> uploadMediaResponse) {
        TweetParameters tweetParameters1 = builder.media(TweetParameters.Media.builder().mediaIds(Arrays.asList(uploadMediaResponse.get().getMediaId())).build()).build();
        twitterClient.postTweet(tweetParameters1);
    }

    private void postTextTweet(String tweetText) {
        String translatedTweet = translateTweet(tweetText);
        String truncatedTranslatedTextTweet = truncateString(translatedTweet, 120);
        TweetParameters tweetParameters = TweetParameters.builder().text(truncatedTranslatedTextTweet).build();
        Tweet tweet = twitterClient.postTweet(tweetParameters);
    }

    private String translateTweet(String originalTweet) {
        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("tr");
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem(originalTweet));

        List<TranslatedTextItem> translations = textTranslationClient.translate(targetLanguages, content, null, from, TextType.PLAIN, null, ProfanityAction.NO_ACTION, ProfanityMarker.ASTERISK, false, false, null, null, null, false);
        return translations.get(0).getTranslations().get(0).getText();
    }

    private String savePhotoToResources(List<PhotoSize> photos){
        PhotoSize photo = photos.get(photos.size() - 1);
        String id = photo.getFileId();
        String getID=null;
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(id);
            String filePath = execute(getFile).getFilePath();

            getID = photo.getFileId();
            java.io.File file = new java.io.File("./resources/telgramphoto/" + getID + ".jpg");
            downloadFile(filePath, file);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return getID;
    }

    @Override
    public void clearWebhook() {

    }
}

