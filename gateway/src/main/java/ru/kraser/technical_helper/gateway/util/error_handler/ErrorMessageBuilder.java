package ru.kraser.technical_helper.gateway.util.error_handler;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessageBuilder {
    public String identifyServer(String message) {
        if (message.contains("12333")) {
            return "Main-Server !!!";
        } else {
            return "!!!";
        }
    }

    public String identifyNotValidArgument(String message) {
        int beginIndex = message.lastIndexOf("default message [") + 17;
        int endIndex = message.lastIndexOf(".") + 1;
        return message.substring(beginIndex, endIndex);
    }
}