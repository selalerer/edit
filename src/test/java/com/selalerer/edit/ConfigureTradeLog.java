package com.selalerer.edit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

public class ConfigureTradeLog implements BeforeAllCallback {

    private static boolean configured = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!configured) {
            configureLogLevel(Level.TRACE);
            configured = true;
        }
    }

    public static void configureLogLevel(Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(level);
    }
}
