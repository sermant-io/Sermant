package com.huawei.apm.core.exception;

import java.util.Locale;

public class DupServiceManager extends RuntimeException {
    public DupServiceManager(String clsName) {
        super(String.format(Locale.ROOT, "Found more than one implement of %s. ", clsName));
    }
}
