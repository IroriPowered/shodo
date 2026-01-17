package cc.irori.shodo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BuiltInFontData implements FontData {

    private static final GlyphMeta DEFAULT_GLYPH = new GlyphMeta(null, 0, 0, 16);

    private final double scale;

    private Map<Character, GlyphMeta> glyphs = new HashMap<>();

    public BuiltInFontData(double scale) {
        this.scale = scale;
    }

    public void load() {
        glyphs.clear();

        URL resource = getClass().getResource("/charmap.json");
        try {
            String contents = new String(resource.openStream().readAllBytes());
            JsonObject json = JsonParser.parseString(contents).getAsJsonObject();

            for (Map.Entry<String, JsonElement> atlasEntry : json.entrySet()) {
                String atlas = atlasEntry.getKey();
                JsonObject atlasJson = atlasEntry.getValue().getAsJsonObject();

                for (Map.Entry<String, JsonElement> glyphEntry : atlasJson.entrySet()) {
                    String key = glyphEntry.getKey();
                    JsonObject glyphJson = glyphEntry.getValue().getAsJsonObject();

                    char c = (char) Integer.parseInt(key.substring(1), 16);
                    glyphs.put(c, new GlyphMeta(
                            atlas,
                            glyphJson.get("x").getAsInt(),
                            glyphJson.get("y").getAsInt(),
                            glyphJson.get("width").getAsInt()
                    ));
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            Logs.logger().atSevere().log("Failed to load built-in font data", e);
        }
    }

    @Override
    public GlyphMeta getGlyph(char c) {
        return glyphs.getOrDefault(c, DEFAULT_GLYPH);
    }

    @Override
    public int getTileSize() {
        return 16;
    }

    @Override
    public int getAtlasSize() {
        return 512;
    }

    @Override
    public int getLineHeight() {
        return getTileSize() + 2;
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
