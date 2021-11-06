package com.huawei.service;

import com.huawei.EmergencyDrillApplication;
import com.huawei.emergency.service.UserAdminCache;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EmergencyDrillApplication.class)
public class TestService {
    private HttpServletRequest request;

    @Before
    public void before(){
        /*request.getSession().get
        UserAdminCache.userMap.put("token",)*/
    }
}
