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
import HTTPClient.NVPair;
import HTTPClient.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.grinder.plugin.http.HTTPRequest;
import org.ngrinder.model.PerfApiCheckPoint;
import org.ngrinder.model.PerfApiVariable;
import org.ngrinder.model.PerfScene;
import org.ngrinder.model.PerfSceneApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 场景调试服务
 * Created by x00377290 on 2019/4/23.
 */
public class PreFlowExecutor {

	private  static  Logger logger = LoggerFactory.getLogger(PreFlowExecutor.class);
	/**
	 * 存储运行时上下文变量
	 */
	public Map<String,String> contextVariables = new ConcurrentHashMap();

	public static HTTPRequest request;

	private PerfScene perfScene;

	private static Long threadId  = Thread.currentThread().getId();

	public PreFlowExecutor(String sceneJson) {
		this.perfScene = StringToObject(sceneJson,PerfScene.class);
	}

	/**
	 * 场景执行
	 */
	public void doExecute() throws Exception {
		    logger.info(threadId +" -- > Start ececute scene "+perfScene.getSceneName());
		    List<ApiResult> apiResults = new LinkedList<>();
	     	for(PerfSceneApi  api : perfScene.getPerfSceneApis()){
				logger.info(threadId +" -- > Start ececute api "+api.getApiName());
				ApiResult apiResult = new ApiResult();
				/**
				 * 4、判断API 是否有参数值传递 并进行变量替换
				 * (1)  URL 中  (2) Body 中 (3)header 中
				 */
				doVartranslate(api);
				 //1、执行API
				HTTPResponse response = executeApi(api);
				//2、判断API 是否存在检查点并执行检查点
				List<CheckPointResult> checkPointResults = new LinkedList<>();
				for(PerfApiCheckPoint checkPoint : api.getPerfApiCheckPoints()){
					 boolean checkResult = checkResult(response,checkPoint);
					 CheckPointResult checkPointResult = new CheckPointResult();
					 checkPointResults.add(checkPointResult);
					 apiResult.setCheckPointResultList(checkPointResults);
				};
				//3、判断API 请求参数中是否有定义上下文变量 进行上下文变量处理
				for(PerfApiVariable variable : api.getPerfApiVariables()){
					  createVariable(response,variable);
				};

				apiResults.add(apiResult);
			}
	}

	/**
	 * 执行API
	 * @return
	 */
	public HTTPResponse executeApi(PerfSceneApi api) throws Exception {
		HTTPResponse response = null;
		request = new HTTPRequest();
		List<NVPair> headerList = new ArrayList<NVPair>();
		headerList.add(new NVPair("Content-Type", ExecuteConstant.contentTypeStr(api.getContentType())));
		NVPair[] headers = (NVPair[]) headerList.toArray();
		request.setHeaders(headers);
		request.setUrl(api.getUrl());
		switch (api.getMethod()){
			case ExecuteConstant.GET:
				response = request.GET();
				break;
			case ExecuteConstant.POST:
				request.setData(getApiBody(api).getBytes());
				response = request.POST();
				break;
			case ExecuteConstant.PUT:
				request.setData(getApiBody(api).getBytes());
				response = request.PUT();
				break;
			case ExecuteConstant.DELETE:
				request.DELETE();
				break;
			case ExecuteConstant.OPTIONS:
				request.setData(getApiBody(api).getBytes());
				response = request.OPTIONS();
				break;
		}
		return response;
	}

	public String getApiBody(PerfSceneApi api){
		int bodyType = api.getRequestBodyType();
		String result ="";
		switch (bodyType){
			case ExecuteConstant.CONTENT_TYPE_XFORM:
				result = objectToJsonString(api.getRequestBodyMap());
				break;
			case  ExecuteConstant.CONTENT_TYPE_RAW:
				result = api.getRequestBodyStr();
				break;
		}
		return result;
	}


	/**
	 * 检查先 check
	 * @param response
	 * @param checkPoint
	 * @return
	 */
	public boolean checkResult(HTTPResponse response,PerfApiCheckPoint checkPoint) throws ParseException, ModuleException, IOException {
        int source = checkPoint.getSource();
		int method = checkPoint.getGetValueMethod();
		String exression = checkPoint.getExpression();
		String expect = checkPoint.getExpect();
		String headerName = checkPoint.getHeaderName();
		int num = checkPoint.getMatchNum();
		String value = StringUtil.getValue(response,source,method,exression,num,headerName);
		//断言方式判断
		if (expect.equals(value)){
			return true;
		}else {
			return false;
		}
	}

	/**
	 * 生产运行时上下文变量 并存储到内存
	 * @param response
	 * @param variable
	 */
	public void createVariable(HTTPResponse response,PerfApiVariable variable) throws ParseException, ModuleException, IOException {
		int source = variable.getSource();
		int method = variable.getGetValueMethod();
		String exression = variable.getExpression();
		String headerName = variable.getHeaderName();
		int num = variable.getMatchNum();
		contextVariables.put(variable.getVarName(),StringUtil.getValue(response,source,method,exression,num,headerName));
	}

	/**
	 * 对API URL BODY HEADER 参数信息进行参数替换
	 * @param api
	 * @return
	 */
	private PerfSceneApi doVartranslate(PerfSceneApi api) throws JsonProcessingException {
		logger.info(threadId +" -- > Start do url variable translate api "+api.getApiName()+" "+api.getUrl());
//		grinder.logger.info(threadId +" -- > Start do url variable translate api "+api.getApiName()+" "+api.getUrl());
		String urlNew = doVartranslate(api.getUrl());
		api.setUrl(urlNew);
		logger.info(threadId +" -- > Url variable translate finish api "+api.getApiName()+" "+api.getUrl());
		logger.info(threadId +" -- > Start do body variable translate api "+api.getApiName()+" "+getApiBody(api));
		int bodyType = api.getRequestBodyType();
		switch (bodyType){
			case ExecuteConstant.CONTENT_TYPE_XFORM:
				api.setRequestBodyMap(StringToObject(doVartranslate(getApiBody(api)),ArrayList.class));
				break;
			case  ExecuteConstant.CONTENT_TYPE_RAW:
				api.setRequestBodyStr(doVartranslate(getApiBody(api)));
				break;
		}

		logger.info(threadId +" -- > Body variable translate finish api "+api.getApiName()+" "+getApiBody(api));
		logger.info(threadId +" -- > Start do header variable translate api "+api.getApiName()+" "+api.getRequestHeader());
		String headerTransStr = doVartranslate(objectToJsonString(api.getRequestHeader()));
		api.setRequestHeader(StringToObject(headerTransStr,ArrayList.class));
		logger.info(threadId +" -- > Header variable translate finish api "+api.getApiName()+" "+api.getRequestHeader());
		return  api;
	}

	/**
	 * 替换某个字符串中的变量
	 * @param s
	 * @return
	 */
	public String doVartranslate(String s){
		List<String> l = variables(s);
		for(String s2 : l){
			s.replace("${argus#"+s2+"}", contextVariables.get(s2));
		}
		return   s;
	}
	/**
	 *  获取某个字符串中的参数列表
	 * @param s
	 * @return
	 */
	public List<String> variables(String  s){
		List varList = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		String[] vars = s.split("\\$\\{arguus#");
		for(String var : vars){
			String v = var.split("}")[1];
			varList.add(v);
		}
		return varList;
	}


	public static String objectToJsonString(Object object) {
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = "";
		try {
			jsonString = objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.debug("objectToJsonString failed!");
		}
		return jsonString;
	}

	public static <T> T StringToObject(String content, Class<T> valueType) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return (T) objectMapper.readValue(content, valueType);
		}
		catch (IOException e) {
			logger.info("StringToObject failed!");
			return null;
		}
	}

}
