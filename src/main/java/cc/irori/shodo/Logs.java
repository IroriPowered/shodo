package cc.irori.shodo;

import com.hypixel.hytale.logger.HytaleLogger;

public class Logs {

    private static final String LOGGER_NAME = "Shodo";

    public static HytaleLogger logger() {
        return HytaleLogger.get(LOGGER_NAME);
    }
}
