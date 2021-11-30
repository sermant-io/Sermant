package com.huawei.javamesh.core.lubanops.bootstrap.api;

public class APIService {

    private static JSONAPI jsonApi;

    public static JSONAPI getJsonApi() {
        return jsonApi;
    }

    public static void setJsonApi(JSONAPI jsonApi) {
        APIService.jsonApi = jsonApi;
    }

}
