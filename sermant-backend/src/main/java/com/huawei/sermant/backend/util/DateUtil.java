package com.huawei.sermant.backend.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public String getFormatDate(Long times){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(times);
    }
}
