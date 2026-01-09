package com.example.aipipe;

import java.io.*;
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

        int code = con.getResponseCode();
        // response body가 필요하면 읽어도 됨(지금은 생략)
        return code;
    }
}
