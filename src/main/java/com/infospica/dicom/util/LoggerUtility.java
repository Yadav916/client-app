package com.infospica.dicom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerUtility {

    private final static Map<Class<?>, Logger> mapLoggers = new ConcurrentHashMap<>();

    private LoggerUtility() {
        super();
    }

    public static void log(Class clazz, LogLevel logLevel, String message) {
        Logger logger = LoggerUtility.getLogger(clazz);
        switch(logLevel) {
            case INFO: if(logger.isInfoEnabled()) logger.info(message); break;
            case DEBUG: if(logger.isDebugEnabled()) logger.debug(message); break;
            case TRACE: if(logger.isTraceEnabled()) logger.trace(message); break;
            case WARN: if(logger.isWarnEnabled()) logger.warn(message); break;
            case ERROR: if(logger.isErrorEnabled()) logger.error(message); break;
            case FATAL: {
                Marker marker = MarkerFactory.getMarker("FATAL");
                if(logger.isErrorEnabled(marker))
                    logger.error(marker, message);
                break;
            }
            default:
        }
    }

    public static void log(Class clazz, String message, Throwable t) {
        Logger logger = LoggerUtility.getLogger(clazz);
        if(logger.isErrorEnabled()) logger.error(message, t);
    }

    public static enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }

    private static Logger getLogger(Class clazz) {
        return mapLoggers.computeIfAbsent(clazz, c -> LoggerFactory.getLogger(c.getName()));
    }
}
