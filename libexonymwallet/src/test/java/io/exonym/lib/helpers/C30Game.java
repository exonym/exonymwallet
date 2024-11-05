package io.exonym.lib.helpers;

import java.util.UUID;

public class C30Game {

    String gamma;
    String apiKey;

    public static C30Game init(){
        C30Game game = new C30Game();
        game.setGamma(UUID.randomUUID().toString().replaceAll("-", ""));
        return game;

    }

    public String getGamma() {
        return gamma;
    }

    public void setGamma(String gamma) {
        this.gamma = gamma;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
