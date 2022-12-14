package com.huaweicloud.sermant.injection.controller;

import com.huaweicloud.sermant.injection.dto.WebhookResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * controller单元测试
 *
 * @since 2022-08-01
 */
@SpringBootTest
public class SermantInjectorControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SermantInjectorController controller;

    @Test
    public void injectSermant() throws IOException {
        JsonNode body = mapper.readTree(getClass().getClassLoader().getResourceAsStream("test.json"));
        WebhookResponse response = controller.handleAdmissionReviewRequest((ObjectNode) body);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResponse());
        Assertions.assertTrue(response.getResponse().isAllowed());
    }
}