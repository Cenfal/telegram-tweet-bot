package com.example.monsterdevtelegram;

public class Util {

    public static String truncateString(String input, int maxLength) {
        if (input!=null && input.length() <= maxLength) {
            return input; // return the original string if it's already within the limit
        } else {
            return input.substring(0, maxLength); // return the truncated string
        }
    }
}
