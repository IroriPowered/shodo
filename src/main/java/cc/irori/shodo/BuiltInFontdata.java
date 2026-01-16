package cc.irori.shodo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BuiltInFontdata implements FontData {

    private final double scale;

    private Map<Character, Integer> charWidths = new HashMap<>();

    public BuiltInFontdata(double scale) {
        this.scale = scale;
    }

    public void load() {
        charWidths.clear();

        URL resource = getClass().getResource("/widths.json");
        try {
            String contents = new String(resource.openStream().readAllBytes());
            JsonObject json = JsonParser.parseString(contents).getAsJsonObject();

            for (String key : json.keySet()) {
                char c = (char) Integer.parseInt(key.substring(1), 16);
                int width = json.get(key).getAsInt();
                charWidths.put(c, width);
            }
        } catch (IOException | JsonSyntaxException e) {
            Logs.logger().atSevere().log("Failed to load built-in font data", e);
        }
    }

    @Override
    public int getCharWidth(char c) {
        return charWidths.getOrDefault(c, 16);
    }

    @Override
    public int getLineHeight() {
        return 18;
    }

    @Override
    public double getSpacingWidth() {
        return 2.0;
    }

    @Override
    public double getScale() {
        return scale;
    }
}
