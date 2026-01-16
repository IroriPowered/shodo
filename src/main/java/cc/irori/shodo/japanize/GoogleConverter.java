package cc.irori.shodo.japanize;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class GoogleConverter {

    private static final String API_URL = "http://www.google.com/transliterate?langpair=ja-Hira|ja&text=";

    public static String convert(String input) {
        try {
            URL url = new URL(API_URL + URLEncoder.encode(input, StandardCharsets.UTF_8));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder content;
            try (Stream<String> lines = in.lines()) {
                content = lines.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);
            }
            in.close();
            connection.disconnect();

            JsonElement jsonElement = JsonParser.parseString(content.toString());
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            StringBuilder result = new StringBuilder();
            for (JsonElement element : jsonArray) {
                JsonArray innerArray = element.getAsJsonArray();
                String firstConversionResult = innerArray.get(1).getAsJsonArray().get(0).getAsString();
                result.append(firstConversionResult);
            }

            return result.toString();
        } catch (IOException | JsonSyntaxException e) {
            return input;
        }
    }
}
