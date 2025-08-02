package com.paloma.paloma.javaServer.controllers.accessToken;

import com.paloma.paloma.javaServer.exceptions.UnauthorizedException;

public class GetAccessToken {

    public static String getAccessToken(String authHeader) throws UnauthorizedException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header missing or malformed");

        }
        return authHeader.substring(7);
    }
}
