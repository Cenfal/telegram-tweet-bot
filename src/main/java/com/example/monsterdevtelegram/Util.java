package com.example.monsterdevtelegram;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static List<String> parseSubstrings(String longString) {
        List<String> substrings = new ArrayList<>();

        int length = longString.length();
        int startIndex = 0;
        int endIndex;
        while (startIndex < length) {
            endIndex = Math.min(startIndex + 277, length);
            String substring = longString.substring(startIndex, endIndex);
            // Add three "+" signs to the end of each substring except for the last one
            if (endIndex < length) {
                substring += "+++";
            }
            substrings.add(substring);
            startIndex = endIndex;
        }

        return substrings;
    }

    public static String clearText(String text){
        text=text.replace("&#39;", "'");
        text=text.replace("&quot;", "\"");
        return text;
    }

}
