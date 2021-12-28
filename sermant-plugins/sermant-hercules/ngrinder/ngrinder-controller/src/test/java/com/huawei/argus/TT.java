package com.huawei.argus;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.ngrinder.model.PerfTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import java.util.*;

public class TT {

	private static String getKey(String str) {
		int i = str.indexOf("=");
		int j = i;
		if (i == -1) {
			return null;
		}
		while (true) {
			String tempStr = null;
			if (j - 1 >= 0) {
				tempStr = str.substring(j - 1, j);
			} else {
				break;
			}
			if ("[],".indexOf(tempStr) > -1) {
				break;
			}
			j--;
		}
		return str.substring(j, i);
	}


	private static String getValue(String str) {
		int i = str.indexOf("=");
		int j = i + 1;
		int length = str.length();
		if (i == -1 || j >= length) {
			return null;
		}
		while (true) {
			String tempStr1 = null;
			if (j + 1 < length) {
				tempStr1 = str.substring(j, j + 1);
			} else {
				break;
			}
			if ("]".equals(tempStr1)) {
				break;
			}
			if (",".equals(tempStr1)) {//[label=11,22,66]
				//{"createBy":"User","label":"11"}
				int i1 = str.indexOf(",", j + 1);
				int i2 = str.indexOf("=", j + 1);
				if ((i1 >= i2 && i2 > 0) || (i1 < 0 && i2 > 0)) {
					break;
				}
			}
			j++;
		}
		return str.substring(i + 1, j);
	}


	public static <T> List<T> strToList(String str, String objStr, Class<T> cls) {
		return strToJsonArray(str, objStr).toJavaList(cls);
	}

	public static JSONArray strToJsonArray(String str, String obj) {
		JSONArray array = new JSONArray();
		if ("[".equals(str.substring(0, 1)) && "]".equals(str.substring(str.length() - 1))) {
			while (true) {
				String t1 = str.replaceFirst(obj, "");
				if (t1.indexOf(obj) != -1) {
					String temp = t1.substring(0, t1.indexOf(obj));
					str = t1.substring(t1.indexOf(obj));
					array.add(strToJson(temp));
				} else {
					array.add(strToJson(t1));
					break;
				}
			}
		} else {
			array.add(strToJson(str));
		}
		return array;
	}

	public static JSONObject pageToJson(Page<PerfTest> page) {
		JSONObject result = new JSONObject();
		if (page == null) {
			return result;
		}
		List<PerfTest> content = page.getContent();
		result.put("content", listToJsonArray(Arrays.asList(content.toArray())));
		result.put("total", page.getTotalElements());
		result.put("page", page.getNumber());
		result.put("size", page.getSize());
		JSONArray array = new JSONArray();
		result.put("sort", array);
		Sort sort = page.getSort();
		if (sort != null) {
			Iterator<Sort.Order> iterator = sort.iterator();
			while (iterator.hasNext()) {
				Sort.Order order = iterator.next();
				array.add(order.toString());
			}
		}
		return result;
	}

	public static JSONArray listToJsonArray(List<Object> list) {
		JSONArray array = new JSONArray();
		if (list == null || list.isEmpty()) {
			return array;
		}
		for (Object item : list) {
			array.add(modelStrToJsonStr(item.toString()));
		}
		return array;
	}

	public static JSONObject strToJson(String str) {
		JSONObject result = new JSONObject();
		while (true) {
			String before = getKey(str);
			String after = getValue(str);
			if (before == null) {
				break;
			}
			if ("null".equals(after) || "<null>".equals(after)) {
				result.put(before, null);
			} else {
				result.put(before, after);
			}
			str = str.replace(before + "=" + after, "");
		}
		return result;
	}

	//测试
	public static void main(String[] args) {
		String str = "Square[width=10,height=dsfs[name=lily,role=tretr[id=1]],addr=fddf[city=wuhan]]";
		String s = modelStrToJsonStr(str);
		System.out.println("*************************");
		System.out.println(s);

		str = "{width=10,height=dsfs[name=lily,role=tretr[id=1]],addr=fddf[city=wuhan]}";
		s = modelStrToJsonStr(str);
		System.out.println("*************************");
		System.out.println(s);
		Map<String, String> map = new HashMap<>();
		map.put("11", "33");
		System.out.println(map);
		str = map.toString();
		s = modelStrToJsonStr(str);
		System.out.println("*************************");
		System.out.println(s);

	}

	@Test
	public void testStrToJson() {
		String str = "[createBy=User,createBy1=User1,label=11,22,88]";
		JSONObject jsonObject = strToJson(str);
		System.out.println(jsonObject);
//		String s = modelStrToJsonStr(str);
//		System.out.println("*************");
//		System.out.println(s);
	}
	public static String modelStrToJsonStr(String str) {
		if (str.contains("{") && str.contains("}")) {
			str = str.replaceAll("\\{", "[");
			str = str.replaceAll("}", "]");
		}
		String keyStr = "Json";
		Map<String, String> allObjInfos = new HashMap<>();
		int count = 0;
		while (str.contains("[")) {
			int startIndex = str.lastIndexOf("[");
			int endIndex = str.indexOf("]", startIndex);
			if (startIndex < endIndex) {
				String objInfo = str.substring(startIndex, endIndex + 1);
				int lastIndexOf = str.substring(0, startIndex).lastIndexOf("=");
				String key = "";
				if (lastIndexOf > 0 && lastIndexOf < startIndex) {
					key = str.substring(lastIndexOf + 1, startIndex);
					String value = strToJson(objInfo).toJSONString();
					allObjInfos.put(keyStr + count, value);
				} else {
					break;
				}
				System.out.println(objInfo);
				str = str.replace(key + objInfo, keyStr + (count++));
			} else {
				break;
			}
		}
		System.out.println("allObjInfos:" + allObjInfos);
		System.out.println("str:" + str);
		JSONObject object = strToJson(str);
		System.out.println("object:" + object.toJSONString());
		String objInfo = object.toJSONString();
		for (int i = count - 1; i >= 0; i--) {
			String key = keyStr + i;
			objInfo = objInfo.replace("\"" + key + "\"", allObjInfos.get(key));
		}
		System.out.println("objInfo:" + objInfo);
		return objInfo;
	}

}
