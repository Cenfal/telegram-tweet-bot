package com.example.monsterdevtelegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MSTranslatorConfig {

    @Value("${microsoft.translator.key}")
    private String microsoftTranslatorKey;

    @Value("${microsoft.translator.region}")
    private String microsoftTranslatorRegion;

    public String getMicrosoftTranslatorKey() {
        return microsoftTranslatorKey;
    }

    public void setMicrosoftTranslatorKey(String microsoftTranslatorKey) {
        this.microsoftTranslatorKey = microsoftTranslatorKey;
    }

    public String getMicrosoftTranslatorRegion() {
        return microsoftTranslatorRegion;
    }

    public void setMicrosoftTranslatorRegion(String microsoftTranslatorRegion) {
        this.microsoftTranslatorRegion = microsoftTranslatorRegion;
    }
}
