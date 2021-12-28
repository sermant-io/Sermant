/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.argus.common;

import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * 时序数据库 InfluxDB 连接
 * @author hwx683090
 *
 */
public class InfluxDBUtil {
    private static InfluxDBUtil Instance = null;
    private static String openurl ;//连接地址
    private static String username ;//用户名
    private static String password ;//密码
    private static String database ;//数据库

    private InfluxDB influxDB;

    private InfluxDBUtil(){
        influxDbBuild();
    }



    /**连接时序数据库；获得InfluxDB**/
    public void influxDbBuild(){
		Resource resource = new ClassPathResource("/influxDB.properties");
		try {
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			setOpenurl(props.getProperty("influxDB.openurl"));
			setDatabase(props.getProperty("influxDB.database"));
		} catch (IOException e) {
			System.out.println("读取配置文件influxDB.properties失败");
			e.printStackTrace();
		}

		influxDB = InfluxDBFactory.connect(openurl,new OkHttpClient.Builder());
    }

    /**
     * 设置数据保存策略
     * defalut 策略名 /database 数据库名/ 30d 数据保存时限30天/ 1 副本个数为1/ 结尾DEFAULT 表示 设为默认的策略
     */
    public void createRetentionPolicy(){
        String command = String.format("CREATE RETENTION POLICY \"%s\" ON \"%s\" DURATION %s REPLICATION %s DEFAULT",
                "defalut", database, "30d", 1);
        this.query(command);
    }

    public static InfluxDBUtil getInstance(){
        if (Instance == null){
            //synchronized (InfluxDBUtil.class){
                if (Instance == null){
                    Instance = new InfluxDBUtil();
                    Instance.createRetentionPolicy();
                }
            //}
        }
        return Instance;
    }

    /**
     * 查询
     * @param command 查询语句
     * @return
     */
    public static QueryResult query(String command){
        return getInstance().influxDB.query(new Query(command, database));
    }

    /**
     * 插入
     * @param tags 标签
     * @param fields 字段
     */
    public static void insert(String measurement, Map<String, String> tags, Map<String, Object> fields){
        Builder builder = Point.measurement(measurement);
        builder.tag(tags);
        builder.fields(fields);

        getInstance().influxDB.write(database, "", builder.build());
    }

    /**
     * 删除
     * @param command 删除语句
     * @return 返回错误信息
     */
    public static String deleteMeasurementData(String command){
        QueryResult result = getInstance().influxDB.query(new Query(command, database));
        return result.getError();
    }

    /**
     * 创建数据库
     * @param dbName
     */
    public static void createDB(String dbName){
        getInstance().influxDB.createDatabase(dbName);
    }

    /**
     * 删除数据库
     * @param dbName
     */
    public static  void deleteDB(String dbName){
        getInstance().influxDB.deleteDatabase(dbName);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOpenurl() {
        return openurl;
    }

    public void setOpenurl(String openurl) {
        this.openurl = openurl;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
