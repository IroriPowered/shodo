package cc.irori.shodo;

public interface FontData {

    GlyphMeta getGlyph(char c);

    int getTileSize();

    int getAtlasSize();

    int getLineHeight();

    double getSpacingWidth();

    double getScale();
}
