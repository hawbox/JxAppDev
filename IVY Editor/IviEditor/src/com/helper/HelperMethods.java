package com.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HelperMethods {
    public String getTextFromFile(String source){
        InputStream urlStream = getClass().getResourceAsStream(source);
        String html = null;
        try (BufferedReader urlReader = new BufferedReader(new InputStreamReader(urlStream)))
        {
            StringBuilder builder = new StringBuilder();
            String row;
            while ((row = urlReader.readLine()) != null) {
                builder.append(row);
            }
            html = builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return html;
    }
}
