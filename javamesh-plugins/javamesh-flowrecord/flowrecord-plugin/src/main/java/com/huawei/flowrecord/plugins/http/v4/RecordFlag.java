package com.huawei.flowrecord.plugins.http.v4;

public class RecordFlag {
    private static final ThreadLocal<Boolean> IS_ENTRY = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_RECORD = new ThreadLocal<>();

    public static void setIsEntry(boolean isEntry) {
        IS_ENTRY.set(isEntry);
    }

    public static boolean getIsEntry(){
        return IS_ENTRY.get();
    }

    public static void setIsRecord(boolean isRecord) {
        IS_RECORD.set(isRecord);
    }

    public static boolean getIsRecord() {
        return IS_RECORD.get();
    }
}
