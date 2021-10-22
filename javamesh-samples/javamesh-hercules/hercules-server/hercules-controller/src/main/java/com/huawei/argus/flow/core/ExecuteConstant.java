package com.huawei.argus.flow.core;

/**
 * Created by x00377290 on 2019/4/23.
 */
public class ExecuteConstant {

	public static final  String POST = "POST";
	public static final  String GET = "GET";
	public static final  String PUT = "PUT";
	public static final  String DELETE = "DELETE";
	public static final  String OPTIONS = "OPTIONS";
    //PROTOCOL
	public static final String PROTOCOL_HTTP = "HTTP";
	public static final String PROTOCOL_HTTPS = "HTTPS";
	//
	public static final String SCENE_TYPE_FLOW = "FLOW";
	public static final String SCENE_TYPE_TRAFFIC = "TRAFFIC";
	public static final String SCENE_TYPE_SCRIPT = "SCRIPT";
	//来自 取值方式  source
	public static final  int SOURCE_BODY = 1;
	public static final  int SOURCE_HEADER = 2;

	//content type
	public static final  int CONTENT_TYPE_XFORM = 1;
	public static final  int CONTENT_TYPE_RAW = 2;

	//body raw type
	public static final  int RAW_TYPE_JSON = 1;
	public static final  int RAW_TYPE_TEXT = 2;

	//get value method
	public static final  int GET_VALVE_METHOD_ALL = 1;//全部
	public static final  int GET_VALVE_METHOD_KEY = 2;//键值对
	public static final  int GET_VALVE_METHOD_JSON = 3;//JSON
	public static final  int GET_VALVE_METHOD_RE = 4;//正则表达式
	public static final  int GET_VALVE_METHOD_TEXT = 5;//文本

	//
	public static String contentTypeStr(int type){
		switch (type){
			case CONTENT_TYPE_XFORM:
				return "application/x-www-form-urlencoded";
			case CONTENT_TYPE_RAW:
				return "text/plain";
		}
		return "";
	}

}
