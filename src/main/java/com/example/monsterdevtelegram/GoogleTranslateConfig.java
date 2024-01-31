package com.example.monsterdevtelegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class GoogleTranslateConfig {

    @Value("${google.translator.projectName}")
    private String googleTranslateProjectName;

    @Value("${google.translator.apiKey}")
    private String googleTranslateApiKey; //only for Basic

    public String getGoogleTranslateProjectName() {
        return googleTranslateProjectName;
    }

    public void setGoogleTranslateProjectName(String googleTranslateProjectName) {
        this.googleTranslateProjectName = googleTranslateProjectName;
    }

    public String getGoogleTranslateApiKey() {
        return googleTranslateApiKey;
    }

    public void setGoogleTranslateApiKey(String googleTranslateApiKey) {
        this.googleTranslateApiKey = googleTranslateApiKey;
    }
}
