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

package com.huawei.argus.flow.core;

import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;
import HTTPClient.ParseException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.jayway.jsonpath.JsonPath;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by x00377290 on 2019/4/25.
 */
public class StringUtil {

	public static String getValue(HTTPResponse response,int source, int method, String exression,int matchNum,String headerName) throws IOException, ModuleException, ParseException {
		String result = "";
		String body = response.getText();
        switch(source){
			case ExecuteConstant.SOURCE_HEADER://Header 取值
				result = response.getHeader(headerName);
				break;
			case ExecuteConstant.SOURCE_BODY://body 取值
				switch (method){
					case ExecuteConstant.GET_VALVE_METHOD_ALL://全部
						result = body;
						break;
					case ExecuteConstant.GET_VALVE_METHOD_JSON://JSON 格式取值
						result = jsonMatch(body,exression);
						break;
					case ExecuteConstant.GET_VALVE_METHOD_TEXT:
						result = textMatch(body,exression,matchNum);
						break;
					case ExecuteConstant.GET_VALVE_METHOD_RE://正则表达式取值
						result = regMatch(body,exression,matchNum);
						break;
				}
				break;
		}
		return result;
	}

	/**
	 *
	 * @param withinText  输入
	 * @param regString 正则表达式
	 * @param num 去第几个匹配
	 * @return
	 */
	public static String regMatch(String withinText, String regString,int num) {
		String code = null;
		Pattern pattern = Pattern.compile(regString);
		Matcher matcher = pattern.matcher(withinText);
		if (matcher.find()) {
			matcher.reset();
			while (matcher.find()) {
				code = matcher.group(num);
			}
		}
		return code;
	}

	/**
	 * 通过 Json 表达式获取Json 返回值
	 * @param withinText
	 * @param exression
	 * @return
	 */
	public static String jsonMatch(String withinText,String exression){
		String getValue = JsonPath.read(withinText,exression);
		if(getValue.contains("[")){
			getValue=getValue.substring(getValue.indexOf("[")+1,getValue.length()-1);
			         }
		return getValue.replaceAll("\"","");
	}

	public static String textMatch(String withinText,String exression,int matchNum){
		Document document = Jsoup.parse(withinText);
		Elements elements = document.select(exression);
		Element e = elements.get(matchNum);
		return e.data();
	}
}
