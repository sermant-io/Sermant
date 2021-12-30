package com.huawei.flowrecord.utils;

import com.huawei.flowrecord.domain.RecordJob;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

public class RecordUtil {

    public static boolean isRecord(RecordJob recordJob, String httpMethod, String path) throws IOException {
        if (recordJob == null) {
            return false;
        }

        if (!recordJob.isTrigger()) {
            return false;
        }

        if (!recordJob.getApplication().equals(AppNameUtil.getAppName())) {
            return false;
        }

        InetAddress addr = InetAddress.getLocalHost();
        String localaddr = addr.getHostAddress();

        if (!recordJob.getMachineList().contains(localaddr)) {
            return false;
        }

        if (!recordJob.getMethodList().isEmpty() && !recordJob.getMethodList().contains(httpMethod + " " + path)) {
            return false;
        }

        Date date = new Date();
        if (recordJob.getStartTime().before(date) && recordJob.getEndTime().after(date)) {
            return true;
        } else {
            return false;
        }
    }
}
