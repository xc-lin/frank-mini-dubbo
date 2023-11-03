package com.lxc.dubbo.core.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtil {

    private static final String logPrefix = "【----Frank-mini-dubbo----】:";

    public static void error(String format, Object... args) {
        log.error(logPrefix + format, args);
    }

    public static void info(String format, Object... args) {
        log.info(logPrefix + format, args);
    }

    public static void debug(String format, Object... args) {
        log.debug(logPrefix + format, args);
    }
}
