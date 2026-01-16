package cc.irori.shodo.japanize;

import javax.annotation.Nullable;

public class Japanizer {

    private static final String JAPANIZE_APPLICABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.,!?{}[]()<>+-*/\\\"'`^#$%&~|:;@ ";
    private static final String JAPANIZE_TRIGGER = "abcdefghijklmnopqrstuvwxyz";

    // Private constructor to prevent instantiation
    private Japanizer() {
    }

    public static boolean isJapanizeApplicable(String input) {
        for (char c : input.toCharArray()) {
            if (JAPANIZE_APPLICABLE.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldStartJapanize(String input) {
        for (char c : input.toCharArray()) {
            if (JAPANIZE_TRIGGER.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    public static @Nullable String japanizeString(String input) {
        return japanizeString(input, false);
    }

    public static @Nullable String japanizeString(String input, boolean force) {
        String japanized = input;
        if (!force && (!shouldStartJapanize(input) || !isJapanizeApplicable(input))) {
            return null;
        }

        try {
            japanized = YukiKanaConverter.conv(japanized);
            japanized = GoogleConverter.convert(japanized);
        } catch (Throwable ignored) {
        }

        if (japanized.equals(input)) {
            return null;
        }

        return japanized;
    }
}
