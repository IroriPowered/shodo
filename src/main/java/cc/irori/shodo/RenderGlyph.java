package cc.irori.shodo;

import javax.annotation.Nullable;
import java.awt.*;

public record RenderGlyph(char character, double x, double y, @Nullable Color color) {
}
