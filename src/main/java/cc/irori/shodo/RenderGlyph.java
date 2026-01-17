package cc.irori.shodo;

import javax.annotation.Nullable;
import java.awt.*;

public record RenderGlyph(char character, GlyphMeta meta, double x, double y, @Nullable Color color) {
}
