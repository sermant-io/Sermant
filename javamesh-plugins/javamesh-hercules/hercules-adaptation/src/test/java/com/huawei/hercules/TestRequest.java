package com.huawei.hercules;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class TestRequest {

    private static final String host = "http://localhost:8089/hercules_controller_war";

    private static final String url = host + "/rest/script/uploadFile";

    public static void main(String[] args) {

        String fileName = "D:\\tmp\\ngrinder\\.ngrinder\\perftest\\0_999\\74\\dist\\TestRunner.groovy";
        Map<String, Object> map = new HashMap<>();
        map.put("path", "");
        map.put("description", "Admin");

        try {
            uploadFile(fileName, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadFile(String fileName, Map<String, Object> map) throws Exception {
        // 换行符
        final String newLine = "\r\n";
        final String boundaryPrefix = "--";
        // 定义数据分隔线
        String BOUNDARY = "========7d4a6d158c9";
        // 服务器的域名
        URL url = new URL(TestRequest.url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // 设置为POST情
        conn.setRequestMethod("POST");
        // 发送POST请求必须设置如下两行
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        // 设置请求头参数
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        try (
                OutputStream outputStream = conn.getOutputStream();
                DataOutputStream out = new DataOutputStream(outputStream);
        ) {
            //传递参数

            if (map != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    stringBuilder.append(boundaryPrefix)
                            .append(BOUNDARY)
                            .append(newLine)
                            .append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey())
                            .append("\"").append(newLine).append(newLine)
                            .append(String.valueOf(entry.getValue()))
                            .append(newLine);
                }
                out.write(stringBuilder.toString().getBytes(Charset.forName("UTF-8")));
            }

            // 上传文件
            {
                File file = new File(fileName);
                StringBuilder sb = new StringBuilder();
                sb.append(boundaryPrefix);
                sb.append(BOUNDARY);
                sb.append(newLine);
                sb.append("Content-Disposition: form-data;name=\"file\";filename=\"").append(fileName)
                        .append("\"").append(newLine);
                sb.append("Content-Type:application/octet-stream");
                sb.append(newLine);
                sb.append(newLine);
                out.write(sb.toString().getBytes());

                try (
                        DataInputStream in = new DataInputStream(new FileInputStream(file));
                ) {
                    byte[] bufferOut = new byte[1024];
                    int bytes = 0;
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    out.write(newLine.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 定义最后数据分隔线，即--加上BOUNDARY再加上--。
            byte[] end_data = (newLine + boundaryPrefix + BOUNDARY + boundaryPrefix + newLine)
                    .getBytes();
            // 写上结尾标识
            out.write(end_data);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //定义BufferedReader输入流来读取URL的响应
        try (
                InputStream inputStream = conn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
