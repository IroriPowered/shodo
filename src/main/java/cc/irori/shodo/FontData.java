package cc.irori.shodo;

public interface FontData {

    int getCharWidth(char c);

    int getLineHeight();

    double getSpacingWidth();

    double getScale();
}
