package com.xxx.cloud.auth.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IdGenerator {

    public static String generateAuthorizationId(String id) {
        String currentTime = DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSS").format(LocalDateTime.now());
        return id + currentTime;
    }

}
