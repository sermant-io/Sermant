package com.huawei.javamesh.core.lubanops.bootstrap.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogFactory {

    private static Logger logger = null;

    private static boolean methodLogError = false;

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger lOG) {
        logger = lOG;
    }

    public static void log(Throwable e) {
        if (!methodLogError) {
            getLogger().log(Level.SEVERE, "", e);
            methodLogError = true;
        }
    }

}
