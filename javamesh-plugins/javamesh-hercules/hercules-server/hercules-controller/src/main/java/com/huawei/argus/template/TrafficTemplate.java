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

package com.huawei.argus.template;

/**
 * @Author: j00466872
 * @Date: 2019/5/11 11:50
 */
public class TrafficTemplate {
	public static String VALUE = "import static net.grinder.script.Grinder.grinder\n" +
		"import static org.junit.Assert.*\n" +
		"import static org.hamcrest.Matchers.*\n" +
		"import net.grinder.plugin.http.HTTPRequest\n" +
		"import net.grinder.plugin.http.HTTPPluginControl\n" +
		"import net.grinder.script.GTest\n" +
		"import net.grinder.script.Grinder\n" +
		"import net.grinder.scriptengine.groovy.junit.GrinderRunner\n" +
		"import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess\n" +
		"import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread\n" +
		"import org.junit.Before\n" +
		"import org.junit.BeforeClass\n" +
		"import org.junit.Test\n" +
		"import org.junit.runner.RunWith\n" +
		"\n" +
		"import java.util.Date\n" +
		"import java.util.List\n" +
		"import java.util.ArrayList\n" +
		"\n" +
		"import HTTPClient.Cookie\n" +
		"import HTTPClient.CookieModule\n" +
		"import HTTPClient.HTTPResponse\n" +
		"import HTTPClient.NVPair\n" +
		"import groovy.json.JsonSlurper\n" +
		"import groovy.json.JsonOutput\n" +
		"import java.util.Random\n" +
		"\n" +
		"@RunWith(GrinderRunner)\n" +
		"class TestRunner {\n" +
		"\tpublic static GTest test\n" +
		"\tpublic static HTTPRequest request\n" +
		"\tpublic static NVPair[] headers = []\n" +
		"\tpublic static NVPair[] params = []\n" +
		"\tpublic static Cookie[] cookies = []\n" +
		"\tpublic static traffic_proportion = %s\n" +
		"\tpublic static traffic_host = %s\n" +
		"\tpublic static count_sum = %d\n" +
		"\tpublic static count_num\n" +
		"\tpublic static random = new Random()\n" +
		"\tpublic static jsonSlurper = new JsonSlurper()\n" +
		"\tpublic static jsonOutput = new JsonOutput()\n" +
		"\tpublic static traffics\n" +
		"\tpublic static api\n" +
		"\tpublic static body\n" +
		"\tpublic static method\n" +
		"\tpublic static random_num\n" +
		"\tpublic static HTTPResponse result\n" +
		"\tpublic static body_length = 0\n" +
		"\tpublic static token\n" +
		"\n" +
		"\t@BeforeProcess\n" +
		"\tpublic static void beforeProcess() {\n" +
		"\t\tHTTPPluginControl.getConnectionDefaults().timeout = 6000\n" +
		"\t\ttest = new GTest(1, \"script_generate\")\n" +
		"\t\trequest = new HTTPRequest()\n" +
		"\t\tgrinder.logger.info(\"before process.\")\n" +
		"\t}\n" +
		"\n" +
		"\t@BeforeThread\n" +
		"\tpublic void beforeThread() {\n" +
		"\t\ttest.record(this, \"test\")\n" +
		"\t\tgrinder.statistics.delayReports=true;\n" +
		"\t\tgrinder.logger.info(\"before thread.\");\n" +
		"\t}\n" +
		"\n" +
		"\t@Before\n" +
		"\tpublic void before() {\n" +
		"\t\trandom_num = random.nextInt(count_sum) + 1\n" +
		"\t\tcount_num = 0\n" +
		"\t\ttraffics = jsonSlurper.parseText(traffic_proportion)\n" +
		"\t\tfor (traffic in traffics){\n" +
		"\t\t\tif ((random_num > count_num) & (random_num <= (count_num + traffic.count))){\n" +
		"\t\t\t\tapi = traffic_host + traffic.traffic_url\n" +
		"\t\t\t\tmethod = traffic.http_method\n" +
		"\t\t\t\tif (method == 'POST' || method == 'PUT'){\n" +
		"\t\t\t\t\tbody_length = 0\n" +
		"\t\t\t\t\tfor (i in traffic.request_body){\n" +
		"\t\t\t\t\t\tbody_length = body_length + 1\n" +
		"\t\t\t\t\t}\n" +
		"\t\t\t\t\tbody = traffic.request_body[random.nextInt(body_length)]\n" +
		"\t\t\t\t\tbody = jsonOutput.toJson(body)\n" +
		"\t\t\t\t}\n" +
		"\t\t\t\ttoken = traffic.token\n" +
		"\t\t\t\tbreak\n" +
		"\t\t\t}\n" +
		"\t\t\tcount_num = count_num + traffic.count\n" +
		"\t\t}\n" +
		"\t\tList<NVPair> headerList = new ArrayList<NVPair>()\n" +
		"\t\theaderList.add(new NVPair(\"X-Auth-Token\", token))\n" +
		"\t\tif (method == 'POST' || method == \"PUT\"){\n" +
		"\t\t\theaderList.add(new NVPair(\"Content-Type\", \"application/json\"))\n" +
		"\t\t}\n" +
		"\t\theaders = headerList.toArray()\n" +
		"\t\trequest.setHeaders(headers)\n" +
		"\t\tcookies.each { CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext()) }\n" +
		"\t\tgrinder.logger.info(\"before thread. init headers and cookies\");\n" +
		"\t}\n" +
		"\n" +
		"\t@Test\n" +
		"\tpublic void test(){\n" +
		"\t\tif (method == \"GET\"){\n" +
		"\t\t\tresult = request.GET(api, params)\n" +
		"\t\t}\n" +
		"\t\telse if (method == \"DELETE\"){\n" +
		"\t\t\tresult = request.DELETE(api, params)\n" +
		"\t\t}\n" +
		"\t\telse if (method == \"POST\"){\n" +
		"\t\t\tresult = request.POST(api, body.getBytes())\n" +
		"\t\t}\n" +
		"\t\telse if (method == \"PUT\"){\n" +
		"\t\t\tresult = request.PUT(api, body.getBytes())\n" +
		"\t\t}\n" +
		"\n" +
		"\t\tif (result.statusCode == 301 || result.statusCode == 302) {\n" +
		"\t\t\tgrinder.logger.warn(\"Warning. The response may not be correct. The response code was {}.\", result.statusCode);\n" +
		"\t\t} else {\n" +
		"\t\t\tassertThat(result.statusCode, is(200));\n" +
		"\t\t}\n" +
		"\t}\n" +
		"}";
}
