package com.example.monsterdevtelegram;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.MediaCategory;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;
import io.github.redouane59.twitter.dto.tweet.UploadMediaResponse;

import static com.example.monsterdevtelegram.Util.parseSubstrings;

@Component
public class MyAmazingBot extends TelegramLongPollingBot {
    Logger logger = Logger.getLogger(MyAmazingBot.class.getName());
    @Autowired
    TwitterClient twitterClient;
    @Autowired
    TextTranslationClient textTranslationClient;
    @Autowired
    Translate googleTranslate;

    @Autowired
    TranslateText translateText;
    @Value("${telegram.bot.username}")
    private String telegramBotUsername;
    @Value("${telegram.bot.token}")
    private String telegramBotToken;
    @Value("${google.translator.projectName}")
    private String googleProjectName;

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
            onUpdateReceived(updates.get(0));
        else {
            List<String> uploadMediaResponseList = new ArrayList<>();
            for (Update update : updates) {
                if (messageHasMedia(update)) {
                    AbstractMap.SimpleEntry mediaIdAndCategory = saveMediaToResources(update);
                    Optional<UploadMediaResponse> uploadMediaResponse = uploadTwitterChunkedMedia(mediaIdAndCategory);
                    uploadMediaResponse.ifPresent(mediaResponse -> uploadMediaResponseList.add(mediaResponse.getMediaId()));
                }
            }
            String caption = updates.get(0).getChannelPost().getCaption();
            if (caption != null) {
                String translatedTweet = translateTweet(caption);
                Util.clearText(translatedTweet);
                postTweetFloodWithMedia(uploadMediaResponseList, translatedTweet);
            } else {
                postTweet(TweetParameters.builder(), uploadMediaResponseList);
            }

        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.log(Level.INFO, "Update received with update id: " + update.getUpdateId() + " and media group id: " + update.getChannelPost().getMediaGroupId());
        String getID = null;
        Optional<UploadMediaResponse> uploadMediaResponse;
        String messageText = Objects.nonNull(update.getChannelPost().getText()) ? update.getChannelPost().getText() : update.getChannelPost().getCaption();

        try {
            if (messageHasMedia(update) && messageText!=null) {
                AbstractMap.SimpleEntry mediaIdAndCategory = saveMediaToResources(update);
                uploadMediaResponse = uploadTwitterChunkedMedia(mediaIdAndCategory);
                String translatedTweet = translateTweet(messageText);
                postTweetFloodWithMedia(Collections.singletonList(uploadMediaResponse.map(UploadMediaResponse::getMediaId).orElse(null)), translatedTweet);
            } else if (messageHasMedia(update) && messageText==null){
                AbstractMap.SimpleEntry mediaIdAndCategory = saveMediaToResources(update);
                uploadMediaResponse = uploadTwitterChunkedMedia(mediaIdAndCategory);
                postTweetMediaOnly(Collections.singletonList(uploadMediaResponse.map(UploadMediaResponse::getMediaId).orElse(null)));
            } else {
                messageText = Objects.nonNull(update.getChannelPost().getText()) ? update.getChannelPost().getText() : update.getChannelPost().getCaption();
                String translatedTweet = translateTweet(messageText);
                postTextTweet(translatedTweet);
            }

        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                FileUtils.cleanDirectory(new java.io.File("./resources/telegramphoto/"));
                FileUtils.cleanDirectory(new java.io.File("./resources/telegramvideo/"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean messageHasMedia(Update update) {
        return (update.getChannelPost().getPhoto() != null && update.getChannelPost().getPhoto().size() > 0) || (update.getChannelPost().getPhoto() != null);
    }

    private void postTweetFloodWithMedia(List<String> uploadMediaResponseList, String translatedTweet) {
        List<String> parsedTranslatedTweet = parseSubstrings(translatedTweet);
        String tweetId = null;
        for (int i = 0; i < parsedTranslatedTweet.size(); i++) {
            String truncatedTranslatedTextTweet = parsedTranslatedTweet.get(i);
            List<String> mediaList = (i == 0) ? uploadMediaResponseList : null;
            if (tweetId == null) {//first tweet of flood with photo
                Util.clearText(truncatedTranslatedTextTweet);
                tweetId = postTweet(TweetParameters.builder().text(truncatedTranslatedTextTweet), mediaList);
            } else {//follow up tweets of the flood
                Util.clearText(truncatedTranslatedTextTweet);
                tweetId = postTweet(TweetParameters.builder().text(truncatedTranslatedTextTweet).reply(TweetParameters.Reply.builder().inReplyToTweetId(tweetId).build()), null);

            }
        }
    }
    private void postTweetMediaOnly(List<String> uploadMediaResponseList){
        postTweet(TweetParameters.builder(), uploadMediaResponseList);
    }

    private Optional<UploadMediaResponse> uploadTwitterChunkedMedia(AbstractMap.SimpleEntry mediaIdAndCategory) {
        logger.log(Level.INFO, "TwitterClient upload Chunked Media Call");
        if (mediaIdAndCategory.getValue().equals(MediaCategory.TWEET_IMAGE))
            return twitterClient.uploadChunkedMedia(new java.io.File("./resources/telegramphoto/" + mediaIdAndCategory.getKey() + ".jpg"), MediaCategory.TWEET_IMAGE);
        if (mediaIdAndCategory.getValue().equals(MediaCategory.TWEET_VIDEO))
            return twitterClient.uploadChunkedMedia(new java.io.File("./resources/telegramphoto/" + mediaIdAndCategory.getKey() + ".mp4"), MediaCategory.TWEET_VIDEO);
        else {
            throw new RuntimeException("Media can't be uploaded for media id: " + mediaIdAndCategory.getKey());
        }
    }

    private String postTweet(TweetParameters.TweetParametersBuilder builder, List<String> mediaIdList) {
        String tweetId;
        TweetParameters tweetParameters1;
        try {
            if (mediaIdList != null && !mediaIdList.isEmpty()) {
                tweetParameters1 = builder.media(TweetParameters.Media.builder().mediaIds(mediaIdList).build()).build();

            } else {
                tweetParameters1 = builder.build();
            }
            logger.log(Level.INFO, "TwitterClient Post Tweet Call");

            tweetId = twitterClient.postTweet(tweetParameters1).getId();
            logger.log(Level.INFO, "tweet id: " + tweetId + " tweeted text: " + tweetParameters1.getText());
            return tweetId;

        } catch (RuntimeException e) {
            logger.log(Level.INFO, "there is an issue with post tweet");
            throw new RuntimeException(e.getMessage());
        } finally {
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
        if (originalTweet == null) {
            return null;
        }
        String from = "en";
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("tr");
        List<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem(originalTweet));
        List<TranslatedTextItem> translations;
        Translation translation;
        String translationGoogleTranslateAdvanced;
        try {
            logger.log(Level.INFO, "TextTranslationClient Translate Call");
            //Bing translation
            //translations = textTranslationClient.translate(targetLanguages, content, null, from, TextType.PLAIN, null, ProfanityAction.NO_ACTION, ProfanityMarker.ASTERISK, false, false, null, null, null, false);
            //Google Translation Basic
            // translation=googleTranslate.translate(originalTweet, Translate.TranslateOption.targetLanguage("TR"));
            try {
                //Google Translation Advanced
                translationGoogleTranslateAdvanced = translateText.translateText(googleProjectName, "tr", originalTweet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
        //return translations.get(0).getTranslations().get(0).getText();
        return translationGoogleTranslateAdvanced;
    }

    private AbstractMap.SimpleEntry saveMediaToResources(Update update) {
        String getId;
        if (!CollectionUtils.isEmpty(update.getChannelPost().getPhoto())) {
            getId = savePhotoToResources(update.getChannelPost().getPhoto());
            return new AbstractMap.SimpleEntry<>(getId, MediaCategory.TWEET_IMAGE);
        }
        if (update.getChannelPost().getVideo() != null) {
            getId = saveVideoToResources((update.getChannelPost().getVideo()));
            return new AbstractMap.SimpleEntry<>(getId, MediaCategory.TWEET_VIDEO);
        }
        throw new RuntimeException("Media can't save for update id: " + update.getUpdateId());

    }

    private String saveVideoToResources(Video video) {
        String id = video.getFileId();
        String getID;
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(id);
            String filePath = execute(getFile).getFilePath();

            getID = video.getFileId();
            java.io.File file = new java.io.File("./resources/telegramvideo/" + getID + ".mp4");
            downloadFile(filePath, file);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e.getMessage());
        }
        return getID;
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
            java.io.File file = new java.io.File("./resources/telegramphoto/" + getID + ".jpg");
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

