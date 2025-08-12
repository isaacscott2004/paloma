package com.paloma.paloma.javaServer.exceptions;

public class NoDailyCheckinsFoundException extends RuntimeException {
    public NoDailyCheckinsFoundException() {
        super("No daily checkins found for user");
    }
}
