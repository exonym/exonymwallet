package io.exonym.lib.lite;

import okhttp3.*;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Http {

    private static final Logger logger = Logger.getLogger(Http.class.getName());

    private static class InMemoryCookieJar implements CookieJar {

        private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url, new ArrayList<>(cookies)); // Save cookies for the URL
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = new ArrayList<>();
            for (HttpUrl storedUrl : cookieStore.keySet()) {
                // Match cookies for the domain
                if (url.host().equals(storedUrl.host())) {
                    cookies.addAll(cookieStore.get(storedUrl));
                }
            }
            return cookies;
        }
    }

    private OkHttpClient client;

    public Http() {
        int timeout = 60;

        // Attach the InMemoryCookieJar to the client
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .cookieJar(new InMemoryCookieJar()) // Attach custom cookie jar
                .build();
    }

    public String basicPost(String url, String json) throws IOException {
        return basicPost(url, json, new HashMap<>());
    }

    public String basicPost(String url, String json, HashMap<String, String> headers) throws IOException {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11")
                .addHeader("Accept", "application/json")
                .addHeader("Accept-Language", "en-US,en;q=0.5")
                .addHeader("Access-Control-Request-Headers", "content-type")
                .addHeader("Access-Control-Request-Method", "POST")
                .addHeader("Method", "POST")
                .addHeader("content-type", "application/json")
                .post(body);

        for (String k : headers.keySet()) {
            requestBuilder.addHeader(k, headers.get(k));
        }

        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public String basicGet(String url) throws IOException {
        return basicGet(url, new HashMap<>());
    }

    public String basicGet(String url, HashMap<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11")
                .addHeader("origin", "http://exonym-node")
                .addHeader("content-type", "application/json")
                .addHeader("Accept", "text/plain")
                .addHeader("Method", "GET")
                .get();

        for (String k : headers.keySet()) {
            requestBuilder.addHeader(k, headers.get(k));
        }

        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public void newContext() {
        // Replace the current client with a new one that has a fresh CookieJar
        OkHttpClient.Builder builder = client.newBuilder();
        builder.cookieJar(new InMemoryCookieJar());
        client = builder.build(); // Reset client with a new CookieJar
    }

}
