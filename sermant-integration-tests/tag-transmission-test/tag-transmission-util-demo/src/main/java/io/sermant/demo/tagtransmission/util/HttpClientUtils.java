/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.demo.tagtransmission.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * httpclient 工具类
 *
 * @author daizhenyu
 * @since 2023-10-12
 **/
public class HttpClientUtils {
    private HttpClientUtils() {
    }

    /**
     * jdkhttp get方法工具类
     *
     * @param url
     * @return http请求的response
     */
    public static String doHttpUrlConnectionGet(String url) {
        String responseContext = null;
        BufferedReader in = null;
        HttpURLConnection connection = null;
        try {
            URL serverUrl = new URL(url);
            connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod("GET");

            // 读取响应数据
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            responseContext = content.toString();
        } catch (IOException e) {
            // ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return responseContext;
    }
}
