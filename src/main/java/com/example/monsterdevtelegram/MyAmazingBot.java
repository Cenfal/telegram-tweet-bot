package com.example.monsterdevtelegram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
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

import static com.example.monsterdevtelegram.Util.parseSubstrings;
import static com.example.monsterdevtelegram.Util.truncateString;

@Component
public class MyAmazingBot extends TelegramLongPollingBot {
    Logger logger = Logger.getLogger(MyAmazingBot.class.getName());
    @Autowired
    TwitterClient twitterClient;
    @Autowired
    TextTranslationClient textTranslationClient;
    @Value("${telegram.bot.username}")
    private String telegramBotUsername;
    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Override
    public String getBotUsername() {
        return telegramBotUsername;
    }

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        logger.log(Level.INFO, "Updates multiple let's see");
        if (updates.size() == 1)
            updates.forEach(this::onUpdateReceived);
        else {
            List<String> uploadMediaResponseList = new ArrayList<>();
            for (Update update : updates) {
                if (null != update.getChannelPost().getPhoto()) {
                    String getID = savePhotoToResources(update.getChannelPost().getPhoto());
                    Optional<UploadMediaResponse> uploadMediaResponse = uploadTwitterChunkedMedia(getID);
                    uploadMediaResponse.ifPresent(mediaResponse -> uploadMediaResponseList.add(mediaResponse.getMediaId()));
                }
            }
            String caption = updates.get(0).getChannelPost().getCaption();
            if (caption != null) {
                String translatedTweet = translateTweet(caption);
                //String truncatedTranslatedTextTweet = truncateString(translatedTweet, 280); //will be remove after twitter premium account
                postTweetFloodWithMedia(uploadMediaResponseList, translatedTweet);
            } else {
                postTweet(TweetParameters.builder(), uploadMediaResponseList);
            }

        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.log(Level.INFO, "Update received with update id: " + update.getUpdateId() + " and media group id: " + update.getChannelPost().getMediaGroupId());
        Message message = update.getChannelPost();
        List<PhotoSize> photos = message.getPhoto();
        String getID = null;
        if (photos != null && photos.size() > 0) {
            getID = savePhotoToResources(photos);
        }

        try {
            String tweetText = update.getChannelPost().getText();
            if (tweetText != null) {
                postTextTweet(tweetText);
                return;
            }
            String caption = update.getChannelPost().getCaption();
            if (caption != null) {
                String translatedTweet = translateTweet(caption);
                Optional<UploadMediaResponse> uploadMediaResponse = uploadTwitterChunkedMedia(getID);
                if (uploadMediaResponse.isPresent()) {
                    String truncatedTranslatedTextTweet = truncateString(translatedTweet, 280);
                    //post tweet with media and text
                    postTweet(TweetParameters.builder().text(truncatedTranslatedTextTweet), Collections.singletonList(uploadMediaResponse.get().getMediaId()));

                }
            } else {
                logger.log(Level.INFO, "TwitterClient upload Chunked Media Call");
                Optional<UploadMediaResponse> uploadMediaResponse = uploadTwitterChunkedMedia(getID);
                //post tweet with media-only
                uploadMediaResponse.ifPresent(mediaResponse -> postTweet(TweetParameters.builder(), Collections.singletonList(mediaResponse.getMediaId())));
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                FileUtils.cleanDirectory(new java.io.File("./resources/telegramphoto/"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void postTweetFloodWithMedia(List<String> uploadMediaResponseList, String translatedTweet) {
        List<String> parsedTranslatedTweet = parseSubstrings(translatedTweet);
        Tweet tweet = null;
        for (String truncatedTranslatedTextTweet : parsedTranslatedTweet) {
            if (tweet != null) {
                tweet = postTweet(TweetParameters.builder().text(truncatedTranslatedTextTweet).reply(TweetParameters.Reply.builder().inReplyToTweetId(tweet.getId()).build()), uploadMediaResponseList);
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                tweet = postTweet(TweetParameters.builder().text(truncatedTranslatedTextTweet), uploadMediaResponseList);
            }
        }
    }

    private Optional<UploadMediaResponse> uploadTwitterChunkedMedia(String getID) {
        logger.log(Level.INFO, "TwitterClient upload Chunked Media Call");
        return twitterClient.uploadChunkedMedia(new java.io.File("./resources/telegramphoto/" + getID + ".jpg"), MediaCategory.TWEET_IMAGE);
    }

    private Tweet postTweet(TweetParameters.TweetParametersBuilder builder, List<String> mediaIdList) {
        Tweet tweet = null;
        try {
            TweetParameters tweetParameters1 = builder.media(TweetParameters.Media.builder().mediaIds(mediaIdList).build()).build();
            logger.log(Level.INFO, "TwitterClient Post Tweet Call");

            tweet = twitterClient.postTweet(tweetParameters1);
            return tweet;

        } catch (RuntimeException e) {
            logger.log(Level.INFO, "there is an issue with post tweet");
            throw new RuntimeException(e.getMessage());
        } finally {
            logger.log(Level.INFO, tweet.getText() + " tweeted");
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void postTextTweet(String tweetText) {
        try {
            String translatedTweet = translateTweet(tweetText);
            List<String> parsedTranslatedTweet = parseSubstrings(translatedTweet);
            Tweet tweet = null;
            for (String truncatedTranslatedTextTweet : parsedTranslatedTweet) {
                if (tweet != null) {
                    tweet = twitterClient.postTweet(TweetParameters.builder().text(truncatedTranslatedTextTweet).reply(TweetParameters.Reply.builder().inReplyToTweetId(tweet.getId()).build()).build());

                    TimeUnit.SECONDS.sleep(10);
                } else {
                    tweet = twitterClient.postTweet(truncatedTranslatedTextTweet);
                }
            }
            //TweetParameters tweetParameters = TweetParameters.builder().text(truncatedTranslatedTextTweet).build();
            logger.log(Level.INFO, "TwitterClient Post Text Tweet Call");
            TimeUnit.SECONDS.sleep(10);

        } catch (RuntimeException e) {
            logger.log(Level.INFO, "there is an issue with post tweet");
            throw new RuntimeException(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private String translateTweet(String originalTweet) {
        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("tr");
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem(originalTweet));
        List<TranslatedTextItem> translations;
        try {
            logger.log(Level.INFO, "TextTranslationClient Translate Call");
            translations = textTranslationClient.translate(targetLanguages, content, null, from, TextType.PLAIN, null, ProfanityAction.NO_ACTION, ProfanityMarker.ASTERISK, false, false, null, null, null, false);

        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
        return translations.get(0).getTranslations().get(0).getText();
    }

    private String savePhotoToResources(List<PhotoSize> photos) {
        PhotoSize photo = photos.get(photos.size() - 1);
        String id = photo.getFileId();
        String getID;
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(id);
            String filePath = execute(getFile).getFilePath();

            getID = photo.getFileId();
            java.io.File file = new java.io.File("./resources/telgramphoto/" + getID + ".jpg");
            downloadFile(filePath, file);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e.getMessage());
        }
        return getID;
    }

    @Override
    public void clearWebhook() {

    }
}

