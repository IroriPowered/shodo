package cc.irori.shodo;

import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;

public class AnchorBuilder {

    private Value<Integer> left;
    private Value<Integer> right;
    private Value<Integer> top;
    private Value<Integer> bottom;
    private Value<Integer> height;
    private Value<Integer> full;
    private Value<Integer> horizontal;
    private Value<Integer> vertical;
    private Value<Integer> width;
    private Value<Integer> minWidth;
    private Value<Integer> maxWidth;

    public AnchorBuilder setLeft(Value<Integer> left) {
        this.left = left;
        return this;
    }

    public AnchorBuilder setRight(Value<Integer> right) {
        this.right = right;
        return this;
    }

    public AnchorBuilder setTop(Value<Integer> top) {
        this.top = top;
        return this;
    }

    public AnchorBuilder setBottom(Value<Integer> bottom) {
        this.bottom = bottom;
        return this;
    }

    public AnchorBuilder setHeight(Value<Integer> height) {
        this.height = height;
        return this;
    }

    public AnchorBuilder setFull(Value<Integer> full) {
        this.full = full;
        return this;
    }

    public AnchorBuilder setHorizontal(Value<Integer> horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public AnchorBuilder setVertical(Value<Integer> vertical) {
        this.vertical = vertical;
        return this;
    }

    public AnchorBuilder setWidth(Value<Integer> width) {
        this.width = width;
        return this;
    }

    public AnchorBuilder setMinWidth(Value<Integer> minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public AnchorBuilder setMaxWidth(Value<Integer> maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Value<Integer> getLeft() {
        return left;
    }

    public Value<Integer> getRight() {
        return right;
    }

    public Value<Integer> getTop() {
        return top;
    }

    public Value<Integer> getBottom() {
        return bottom;
    }

    public Value<Integer> getHeight() {
        return height;
    }

    public Value<Integer> getFull() {
        return full;
    }

    public Value<Integer> getHorizontal() {
        return horizontal;
    }

    public Value<Integer> getVertical() {
        return vertical;
    }

    public Value<Integer> getWidth() {
        return width;
    }

    public Value<Integer> getMinWidth() {
        return minWidth;
    }

    public Value<Integer> getMaxWidth() {
        return maxWidth;
    }

    public Anchor build() {
        Anchor anchor = new Anchor();
        anchor.setLeft(left);
        anchor.setRight(right);
        anchor.setTop(top);
        anchor.setBottom(bottom);
        anchor.setHeight(height);
        anchor.setFull(full);
        anchor.setHorizontal(horizontal);
        anchor.setVertical(vertical);
        anchor.setWidth(width);
        anchor.setMinWidth(minWidth);
        anchor.setMaxWidth(maxWidth);
        return anchor;
    }
}
