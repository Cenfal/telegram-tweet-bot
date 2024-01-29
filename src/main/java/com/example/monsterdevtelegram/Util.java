package com.example.monsterdevtelegram;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static String truncateString(String input, int maxLength) {
        if (input != null && input.length() <= maxLength) {
            return input; // return the original string if it's already within the limit
        } else {
            if (input != null) {
                return input.substring(0, maxLength); // return the truncated string
            }
        }
        return input;
    }

    public static List<String> parseSubstrings(String input) {
        List<String> substrings = new ArrayList<>();
        int length = input.length();
        int start = 0;
        int end = Math.min(277, length);

        while (start < length) {
            String substring = input.substring(start, end);
            if (end < length) {
                substring += "+++"; // Append "+++" except for the last substring
            }
            substrings.add(substring);
            start = end;
            end = Math.min(start + 280, length);
        }

        return substrings;
    }

}
