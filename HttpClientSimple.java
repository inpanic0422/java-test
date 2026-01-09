package com.example.aipipe;

import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClientSimple {

    public static int postJson(String url, String jsonBody) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(3000);
        con.setReadTimeout(10000);
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        try (OutputStream os = con.getOutputStream()) {
            os.write(jsonBody.getBytes("UTF-8"));
        }

        return con.getResponseCode();
    }
}
